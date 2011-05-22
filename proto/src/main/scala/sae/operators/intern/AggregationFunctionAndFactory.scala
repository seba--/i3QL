package sae.operators.intern

/**
 * IMPORTANT: clients should NOT implement this interface
 * clients should implement one of this interfaces: 
 *  -NotSelfMaintainalbeAggregationFunction
 *  -SelfMaintainalbeAggregationFunction 
 */
trait AggregationFunction[Domain <: AnyRef, Result] {
    def add(newD : Domain, data : Iterable[Domain]) : Result
    def remove(newD : Domain, data : Iterable[Domain]) : Result
    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) : Result
}
trait DistinctAggregationFunction[Domain <: AnyRef, Result] extends AggregationFunction[Domain, Result] {
}

/**
 * IMPORTANT: clients should NOT implement this interface
 * clients should implement one of this interfaces: 
 *  -NotSelfMaintainalbeAggregationFunctionFactory
 *  -SelfMaintainalbeAggregationFunctionFactory
 *  
 */
trait AggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]{
    def apply() : AggregationFunction[Domain, AggregationValue]
}
trait DistinctAggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any] 
extends AggregationFunctionFactory[Domain,AggregationValue]{}
