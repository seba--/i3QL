package sae.operators

import sae.operators.intern._

trait NotSelfMaintainalbeAggregationFunction[Domain <: AnyRef, Result] extends AggregationFunction[Domain, Result] {
}
trait SelfMaintainalbeAggregationFunction[Domain <: AnyRef, Result] extends AggregationFunction[Domain, Result] {
    final override def add(newD : Domain, data : Iterable[Domain]) : Result = {
        add(newD)
    }
    final override def remove(newD : Domain, data : Iterable[Domain]) : Result = {
        remove(newD)
    }
    final override def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) : Result = {
        update(oldD, newD)
    }
    def add(newD : Domain) : Result
    def remove(newD : Domain) : Result
    def update(oldD : Domain, newD : Domain) : Result
}

trait DistinctAggregationFunction[Domain <: AnyRef, Result] extends AggregationFunction[Domain, Result] {
}
