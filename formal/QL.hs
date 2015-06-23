{-# LANGUAGE GADTs #-}

module QL where

import Data.Maybe
import qualified Data.Map as Map

data QL a b where
     Map :: (a -> b) -> QL [a] [b]
     Filter :: (a -> Bool) -> QL [a] [a]
     Fold :: (a -> b -> b) -> b -> QL [a] b
     Join :: Ord k => (a -> k) -> (b -> k) -> (a -> b -> c) -> QL ([a],[b]) [c]
     

eval :: QL a b -> a -> b
eval (Map f) dat = map f dat
eval (Filter f) dat = filter f dat
eval (Fold f c) dat = foldr f c dat
eval (Join k1 k2 f) (xs,ys) = concat $ Map.elems $ merge f ix1 ix2
     where ix1 = indexed k1 xs
           ix2 = indexed k2 ys

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

data PData a
     = PUnit [a]
     | Partition [PData a]

