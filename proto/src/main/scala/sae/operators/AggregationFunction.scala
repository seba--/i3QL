package sae.operators

import sae.operators.intern._

/**
 * Interface for a not self maintainable aggregation function like min
 */
trait NotSelfMaintainalbeAggregationFunction[Domain <: AnyRef, Result <: Any] extends AggregationFunction[Domain, Result] {

}
/**
 * Interface for a self maintainable aggregation function like count
 */
trait SelfMaintainalbeAggregationFunction[Domain <: AnyRef, Result <: Any] extends AggregationFunction[Domain, Result] {

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

trait DistinctSelfMaintainableAggregationFunction[Domain <: AnyRef, Result <: Any] extends SelfMaintainalbeAggregationFunction[Domain, Result] {
}
trait DistinctNotSelfMaintainableAggregationFunction[Domain <: AnyRef, Result <: Any] extends NotSelfMaintainalbeAggregationFunction[Domain, Result] {
}
