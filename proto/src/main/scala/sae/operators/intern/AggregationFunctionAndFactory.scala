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

