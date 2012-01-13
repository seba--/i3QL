package sae.operators

import sae.operators.intern._

/**
 * Interface for a not self maintainable aggregate function like min
 */
trait NotSelfMaintainalbeAggregateFunction[Domain <: AnyRef, Result <: Any] extends AggregateFunction[Domain, Result]
{

}

/**
 * Interface for a self maintainable aggregate function like count
 */
trait SelfMaintainalbeAggregateFunction[Domain <: AnyRef, Result <: Any] extends AggregateFunction[Domain, Result]
{

    final override def add(newD: Domain, data: Iterable[Domain]): Result = {
        add(newD)
    }

    final override def remove(newD: Domain, data: Iterable[Domain]): Result = {
        remove(newD)
    }

    final override def update(oldD: Domain, newD: Domain, data: Iterable[Domain]): Result = {
        update(oldD, newD)
    }

    def add(newD: Domain): Result

    def remove(newD: Domain): Result

    def update(oldD: Domain, newD: Domain): Result
}


