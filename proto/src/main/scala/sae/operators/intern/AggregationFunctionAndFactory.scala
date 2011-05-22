package sae.operators.intern

trait AggregationFunction[Domain <: AnyRef, Result] {
    def add(newD : Domain, data : Iterable[Domain]) : Result
    def remove(newD : Domain, data : Iterable[Domain]) : Result
    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) : Result
}

trait AggregationFunctionFactory[Domain <: AnyRef, AggregationValue <: Any]{
    def apply() : AggregationFunction[Domain, AggregationValue]
}