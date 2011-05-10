package sae
package functions
import sae.operators._

private class SumIntern[Domain <: AnyRef](val f : Domain => Int) extends AggregationFunktion[Domain, Int] {
    var sum = 0
    def add(d : Domain, data : Iterable[Domain]) = {
        sum += f(d)
        sum
    }
    def remove(d : Domain, data : Iterable[Domain]) = {
        sum -= f(d)
        sum
    }

    def update(oldV : Domain, newV : Domain, data : Iterable[Domain]) = {
        sum -= f(oldV)
        sum += f(newV)
        sum
    }
}
object Sum {
    def apply[Domain <: AnyRef](f : (Domain => Int)) = {
        new AggregationFunktionFactory[Domain, Int] {
            def apply() : AggregationFunktion[Domain, Int] = {
                new SumIntern[Domain](f)
            }
        }
    }
}

