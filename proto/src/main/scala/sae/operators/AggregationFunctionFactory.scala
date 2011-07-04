package sae.operators

import sae.operators.intern._

/**
 * Factory interface for a not self maintainable aggregation function
 */
trait NotSelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends AggregationFunctionFactory[Domain, AggregationValue,NotSelfMaintainalbeAggregationFunction[Domain, AggregationValue]] {

}
/**
 * Factory interface for a self maintainable aggregation function
 */
trait SelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends AggregationFunctionFactory[Domain, AggregationValue,SelfMaintainalbeAggregationFunction[Domain, AggregationValue]] {

}

@Deprecated
trait DistinctSelfMaintainableAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue] {

}
@Deprecated
trait DistinctNotSelfMaintainableAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue] {
}