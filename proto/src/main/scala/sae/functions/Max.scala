package sae.functions

import sae.operators._

private class MaxIntern[Domain <: AnyRef](val f : Domain => Int) extends AggregationFunktion[Domain, Int] {
     var max = Integer.MIN_VALUE
        def add(d : Domain, data : Iterable[Domain]) = {
            if (f(d) > max)
                max = f(d)
            max
        }
        def remove(d : Domain, data : Iterable[Domain]) = {
            if (f(d) == max) {
                max = f(data.first)
                max = (max /: data)((i, s) => if (i > f(s)) i else f(s))
            }
            max
        }

        def update(oldV : Domain, newV : Domain, data : Iterable[Domain]) = {
            if (f(oldV) == max || f(newV) > max) {
                max = f(data.first)
                max = (max /: data)((i, s) => if (i > f(s)) i else f(s))
            }
            max
        }
}

object Max {
    def apply[Domain <: AnyRef](f : (Domain => Int )) = {
        new AggregationFunktionFactory[Domain, Int]{
           def apply() : AggregationFunktion[Domain, Int] = {
               new MaxIntern[Domain](f)
           }
        }
    }
}