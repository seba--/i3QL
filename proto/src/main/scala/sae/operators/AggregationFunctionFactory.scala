package sae.operators

import sae.operators.intern._
trait NotSelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any] 
extends AggregationFunctionFactory[Domain,AggregationValue]{}

trait SelfMaintainalbeAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any] 
extends AggregationFunctionFactory[Domain,AggregationValue]{}

trait DistinctAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any] 
extends AggregationFunctionFactory[Domain,AggregationValue]{}
