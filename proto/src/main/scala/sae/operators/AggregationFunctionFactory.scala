package sae.operators

import sae.operators.intern._
trait NotSelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends AggregationFunctionFactory[Domain, AggregationValue,NotSelfMaintainalbeAggregationFunction[Domain, AggregationValue]] {

}

trait SelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends AggregationFunctionFactory[Domain, AggregationValue,SelfMaintainalbeAggregationFunction[Domain, AggregationValue]] {

}

trait DistinctSelfMaintainableAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue] {

}
trait DistinctNotSelfMaintainableAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue] {
}