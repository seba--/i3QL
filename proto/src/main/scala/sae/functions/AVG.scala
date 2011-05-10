package sae.functions
import sae.operators._

private class AVGIntern[Domain <: AnyRef](val f : Domain => Double) extends AggregationFunktion[Domain, Double] {
    var count = 0
    var sum = 0.0
    def add(d : Domain, data : Iterable[Domain]) = {
        count += 1
        sum += f(d) 
        (sum / count)
    }
    def remove(d : Domain, data : Iterable[Domain]) = {
        count -= 1
        sum -= f(d)
        (sum / count)
    }

    def update(oldV : Domain, newV : Domain, data : Iterable[Domain]) = {
        sum -= f(oldV)
        sum += f(newV)
        (sum / count)
    }
}

object AVG {
    def apply[Domain <: AnyRef](f : (Domain => Double)) = {
        new AggregationFunktionFactory[Domain, Double] {
            def apply() : AggregationFunktion[Domain, Double] = {
                new AVGIntern[Domain](f)
            }
        }
    }
}