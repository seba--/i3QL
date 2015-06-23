{-# LANGUAGE GADTs #-}

module QL where

import Data.Maybe
import qualified Data.Map as Map

-- Query Language
data QL t a b where
     Map :: (a -> b) -> QL t (t a) (t b)
     Filter :: (a -> Bool) -> QL t (t a) (t a)
     Fold :: (a -> b -> b) -> (b -> b -> b) -> b -> QL t (t a) b
     Join :: Ord k => (a -> k) -> (b -> k) -> (a -> b -> c) -> QL t (t a,t b) (t c)
     Localize :: QL t (t a) (t a)
     Materialize :: QL t (t a) (t a)

class Table t where
  tempty :: t a
  tmake :: [a] -> t a
  tconcat :: t a -> t a -> t a
  telems :: t a -> [a]
  eval :: QL t a b -> a -> b


-- # Tables as lists. Non-parallel. Non-incremental.
instance Table [] where
  tempty = []
  tmake = id
  tconcat = (++)
  telems = id

  eval (Map f) dat = map f dat
  eval (Filter f) dat = filter f dat
  eval (Fold f _ c) dat = foldr f c dat
  eval (Join k1 k2 f) (xs,ys) = concat $ Map.elems $ merge f ix1 ix2
       where ix1 = indexed k1 xs
             ix2 = indexed k2 ys
--  eval (IxJoin f m1 m2) _ = concat $ Map.elems $ merge f m1 m2
  eval Localize dat = dat
  eval Materialize dat = dat

indexed :: Ord k => (a -> k) -> [a] -> Map.Map k [a]
indexed kf xs = foldr (\x -> mapExtend (kf x) [x] (x:)) Map.empty xs

merge :: Ord k => (a -> b -> c) -> Map.Map k [a] -> Map.Map k [b] -> Map.Map k [c]
merge f ix1 ix2 = Map.mapWithKey (\k x -> crossApply f x (get k ix2))
                $ Map.filterWithKey (\k _ -> hasKey k ix2) ix1
    where hasKey k ix = isJust $ Map.lookup k ix
          get k ix = fromJust $ Map.lookup k ix

crossApply :: (a -> b -> c) -> [a] -> [b] -> [c]
crossApply f xs ys = [f x y | x <- xs, y <- ys]


-- # Tables distributed over multiple machines. 

-- ID identifies different machines.
type ID = Int

-- Data is partitioned over multiple machines.
data PData t a where
     Partition :: Table t => Map.Map ID (t a) -> PData t a

instance Table t => Table (PData t) where
  tempty = Partition $ Map.singleton 0 tempty
  tmake xs = Partition $ Map.singleton 0 (tmake xs)
  telems (Partition t) = concatMap telems $ Map.elems t
  tconcat (Partition ts1) (Partition ts2) = Partition (Map.unionWith tconcat ts1 ts2)
  
  eval (Map f) (Partition ts) = Partition $ Map.map (eval (Map f)) ts 
  eval (Filter f) (Partition ts) = Partition $ Map.map (eval (Filter f)) ts
  eval (Fold f m c) (Partition ts) = pfold f m c ts
  eval (Join k1 k2 f) (Partition ts1, Partition ts2) = Partition $ pjoin k1 k2 f ts1 ts2
  eval Localize (Partition ts) | Map.null ts = Partition ts
                               | otherwise   = Partition $ plocalize ts
  eval Materialize dat = dat

-- Fold locally on each machine and then merge the respective results.
pfold :: Table t => (a -> b -> b) -> (b -> b -> b) -> b -> Map.Map ID (t a) -> b
pfold f m c ts = Map.foldr m c subres
    where subres = Map.map (eval (Fold f m c)) ts


-- First, join the data that already resides in the same machine (`localJoins`).
-- Second, copy data from all machines `as` to all other machines `bs` (`asCopied`).
-- Third, join the data newly copied data (`nonlocalJoins`).
pjoin :: (Ord k, Table t) => (a -> k) -> (b -> k) -> (a -> b -> c) -> Map.Map ID (t a) -> Map.Map ID (t b) -> Map.Map ID (t c)
pjoin k1 k2 f as bs = localJoins `Map.union` nonlocalJoins
    where localJoins = Map.intersectionWith (curry $ eval (Join k1 k2 f)) as bs
          bkeys = Map.keys bs
          asCopied = Map.foldrWithKey (\ak a res -> foldr (\bk -> if ak /= bk then sendVal a bk else id) res bkeys) Map.empty as
          nonlocalJoins = Map.intersectionWith (curry $ eval (Join k1 k2 f)) asCopied bs

-- Move data from all other machines `ks` to the first machine `k`.
plocalize :: Table t => Map.Map ID (t a) -> Map.Map ID (t a)
plocalize ts = foldr (\from m -> send from k m) ts ks
    where (k:ks) = Map.keys ts


send :: Table t => ID -> ID -> Map.Map ID (t a) -> Map.Map ID (t a)
send from to ts = sendVal t to $ Map.delete from ts
    where t = fromJust $ Map.lookup from ts

sendVal :: Table t => t a -> ID -> Map.Map ID (t a) -> Map.Map ID (t a)
sendVal t to ts = mapExtend to t (tconcat t) ts




-- Utility functions

mapExtend :: Ord k => k -> b -> (b -> b) -> Map.Map k b -> Map.Map k b
mapExtend k noth just m = Map.alter (Just. maybe noth just) k m

