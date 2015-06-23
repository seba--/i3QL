{-# LANGUAGE GADTs #-}

module QL where

import Data.Maybe
import qualified Data.Map as Map

data QL a b where
     Map :: (a -> b) -> QL a b
     Filter :: (a -> Bool) -> QL a a
     Fold :: (a -> b -> b) -> (b -> b -> b) -> b -> QL a b
     Join :: Ord k => (a -> k) -> (b -> k) -> (a -> b -> c) -> QL (a,b) c
--     IxJoin :: Ord k => (a -> b -> c) -> Map.Map k [a] -> Map.Map k [b] -> QL () c
     Localize :: QL a a
     Materialize :: QL a a

class Table t where
  tempty :: t a
  tmake :: [a] -> t a
  tconcat :: t a -> t a -> t a
  telems :: t a -> [a]
  eval :: QL a b -> t a -> t b

instance Table [] where
  tempty = []
  tmake = id
  tconcat = (++)
  telems = id

  eval (Map f) dat = map f dat
  eval (Filter f) dat = filter f dat
  eval (Fold f _ c) dat = [foldr f c dat]
  eval (Join k1 k2 f) dat = concat $ Map.elems $ merge f ix1 ix2
       where (xs,ys) = unzip dat
             ix1 = indexed k1 xs
             ix2 = indexed k2 ys
--  eval (IxJoin f m1 m2) _ = concat $ Map.elems $ merge f m1 m2
  eval Localize dat = dat
  eval Materialize dat = dat

indexed :: Ord k => (a -> k) -> [a] -> Map.Map k [a]
indexed kf [] = Map.empty
indexed kf (x:xs) = mapCons (kf x) x (indexed kf xs)

merge :: Ord k => (a -> b -> c) -> Map.Map k [a] -> Map.Map k [b] -> Map.Map k [c]
merge f ix1 ix2 = Map.mapWithKey (\k x -> crossApply f x (get k ix2))
                $ Map.filterWithKey (\k _ -> hasKey k ix2) ix1
    where hasKey k ix = isJust $ Map.lookup k ix
          get k ix = fromJust $ Map.lookup k ix

crossApply :: (a -> b -> c) -> [a] -> [b] -> [c]
crossApply f xs ys = [f x y | x <- xs, y <- ys]

mapCons :: Ord k => k -> a -> Map.Map k [a] -> Map.Map k [a]
mapCons k x m = case Map.lookup k m of
        Nothing -> Map.insert k [x] m
        Just vs -> Map.insert k (x:vs) m

data PData t a where
     Partition :: Table t => [t a] -> PData t a

instance Table t => Table (PData t) where
  tempty = Partition [tempty]
  tmake xs = Partition [tmake xs]
  telems (Partition t) = concatMap telems t
  tconcat (Partition ts1) (Partition ts2) = Partition (ts1 ++ ts2)
  
  eval (Map f) (Partition ts) = Partition $ map (eval (Map f)) ts 
  eval (Filter f) (Partition ts) = Partition $ map (eval (Filter f)) ts
  eval (Fold f m c) (Partition ts) = Partition [tmake $ [foldr m c subres]]
    where subres = map (head . telems . eval (Fold f m c)) ts
  eval (Join k1 k2 f) (Partition ts) = Partition $ subres ++ crossJoin k1 k2 f ts
    where subres = map (eval (Join k1 k2 f)) ts
  eval Localize (Partition ts) = Partition [eval Localize (foldr tconcat tempty ts)]
  eval Materialize dat = dat

crossJoin :: (a -> k) -> (b -> k) -> (a -> b -> c) -> [t (a,b)] -> [t c]
crossJoin k1 k2 f [] = []
crossJoin k1 k2 f (t:ts) = crossJoin1 t ts ++ crossJoin k1 k2 f ts
    where crossJoin1 t [] = []
          crossJoin1 t (u:us) = crossJoinT t u : crossJoin1 t us

          crossJoinT t u = undefined -- eval (Join k1 k2 f) 
