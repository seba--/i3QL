package sae.operators

import sae.operators.intern._
trait NotSelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends AggregationFunctionFactory[Domain, AggregationValue] {
    override def apply() : NotSelfMaintainalbeAggregationFunction[Domain, AggregationValue]
}

trait SelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends AggregationFunctionFactory[Domain, AggregationValue] {
    override def apply() : SelfMaintainalbeAggregationFunction[Domain, AggregationValue]
}

trait DistinctSelfMaintainableAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue] {
    override def apply() : DistinctSelfMaintainableAggregationFunction[Domain, AggregationValue]
}
trait DistinctNotSelfMaintainableAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]
    extends NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue] {
    override def apply() : DistinctNotSelfMaintainableAggregationFunction[Domain, AggregationValue]
}