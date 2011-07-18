package sae.operators

import sae.operators.intern._

/**
 * Factory interface for a not self maintainable aggregation function
 */
trait NotSelfMaintainalbeAggregateFunctionFactory[Domain <: AnyRef, AggregateValue <: Any]
    extends AggregateFunctionFactory[Domain, AggregateValue,NotSelfMaintainalbeAggregateFunction[Domain, AggregateValue]] {

}
/**
 * Factory interface for a self maintainable aggregation function
 */
trait SelfMaintainalbeAggregateFunctionFactory[Domain <: AnyRef, AggregateValue <: Any]
    extends AggregateFunctionFactory[Domain, AggregateValue,SelfMaintainalbeAggregateFunction[Domain, AggregateValue]] {

}
