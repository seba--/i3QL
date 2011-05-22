package sae.operators


import sae.operators.intern._
object Distinct {
    def apply[Domain <: AnyRef, AggregationValue <: Any]
    (aggFunc : AggregationFunctionFactory[Domain, AggregationValue]) : DistinctAggregationFunctionFactory[Domain,AggregationValue] = {
        new DistinctAggregationFunctionFactory[Domain,AggregationValue]{
            def apply() : DistinctAggregationFunction[Domain,AggregationValue] = {
                new DistinctAggregationFunction[Domain,AggregationValue]{
                    val func = aggFunc()
                    def add(newD : Domain, data : Iterable[Domain]) ={
                        func.add(newD, data)
                    }
                    def remove(newD : Domain, data : Iterable[Domain]) ={
                        func.remove(newD, data)
                    }
                    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
                        func.update(oldD, newD, data)
                    }
                }
            }
        }
    }
}