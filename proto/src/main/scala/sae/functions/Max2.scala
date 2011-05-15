package sae.functions

import sae.operators._

private class MaxIntern2[Domain <: AnyRef,Res](val f : Domain => Int, val f2 : (Option[Domain], Int) => Res) extends AggregationFunktion[Domain, Res] {
     var max = Integer.MIN_VALUE
     var value : Option[Domain] = None
        def add(d : Domain, data : Iterable[Domain]) = {
            if (f(d) > max)
                max = f(d)
            f2(value,max)
        }
        def remove(d : Domain, data : Iterable[Domain]) = {
            if (f(d) == max) {
                max = Integer.MIN_VALUE
                data.foreach( x => {
                  if(f(x) > max){
                      max = f(x)
                      value = Some(x)
                  }
                })

            }
            f2(value,max)
        }

        def update(oldV : Domain, newV : Domain, data : Iterable[Domain]) = {
            if (f(oldV) == max || f(newV) > max) {
                   max = Integer.MIN_VALUE
                data.foreach( x => {
                  if(f(x) > max){
                      max = f(x)
                      value = Some(x)
                  }
                })

            }
             f2(value,max)
        }
}

object Max2 {
    def apply[Domain <: AnyRef,Res <:Any ](f : (Domain => Int ), f2: (Option[Domain], Int) => Res) = {
        new AggregationFunktionFactory[Domain, Res]{
           def apply() : AggregationFunktion[Domain, Res] = {
               new MaxIntern2[Domain,Res](f,f2)
           }
        }
    }
}