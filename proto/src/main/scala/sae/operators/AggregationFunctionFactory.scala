package sae.operators

trait AggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]{
    def apply() : AggregationFunction[Domain, AggregationValue]
}
trait NotSelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any] 
extends AggregationFunctionFactory[Domain,AggregationValue]{}

trait SelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any] 
extends AggregationFunctionFactory[Domain,AggregationValue]{}

trait DistinctAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any] 
extends AggregationFunctionFactory[Domain,AggregationValue]{}
