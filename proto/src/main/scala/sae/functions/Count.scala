package sae.functions
import sae.operators._
private class CountIntern[Domain <: AnyRef]() extends AggregationFunktion[Domain, Int] {
    var count = 0
    def add(d : Domain, data : Iterable[Domain]) = {
        count += 1
        count
    }
    def remove(d : Domain, data : Iterable[Domain]) = {
        count -= 1
        count
    }

    def update(oldV : Domain, newV : Domain, data : Iterable[Domain]) = {
        count
    }
}

object Count {
    def apply[Domain <: AnyRef]() = {
        new AggregationFunktionFactory[Domain, Int] {
            def apply() : AggregationFunktion[Domain, Int] = {
                new CountIntern[Domain]()
            }
        }
    }
}