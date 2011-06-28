package sae.operators.intern

/**
 * IMPORTANT: clients should NOT implement this interface
 * clients should implement:
 *  -NotSelfMaintainalbeAggregationFunction
 *  -SelfMaintainalbeAggregationFunction 
 */
trait AggregationFunction[Domain <: AnyRef, Result] {
    def add(newD : Domain, data : Iterable[Domain]) : Result
    def remove(newD : Domain, data : Iterable[Domain]) : Result
    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) : Result
}


/**
 * IMPORTANT: clients should NOT implement this interface
 * clients should implement:
 *  -NotSelfMaintainalbeAggregationFunctionFactory
 *  -SelfMaintainalbeAggregationFunctionFactory
 *  
 */
trait AggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any, AggregationFunctionType <: AggregationFunction[Domain, AggregationValue]]{
    def apply() : AggregationFunctionType

}

