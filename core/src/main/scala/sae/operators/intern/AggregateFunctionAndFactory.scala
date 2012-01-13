package sae.operators.intern

/**
 * IMPORTANT: clients should NOT implement this interface
 * clients should implement:
 *  -NotSelfMaintainalbeAggregateFunction
 *  -SelfMaintainalbeAggregateFunction
 */
trait AggregateFunction[Domain <: AnyRef, Result]
{
    def add(newD: Domain, data: Iterable[Domain]): Result

    def remove(newD: Domain, data: Iterable[Domain]): Result

    def update(oldD: Domain, newD: Domain, data: Iterable[Domain]): Result
}


/**
 * IMPORTANT: clients should NOT implement this interface
 * clients should implement:
 *  -NotSelfMaintainalbeAggregateFunctionFactory
 *  -SelfMaintainalbeAggregateFunctionFactory
 *
 */
trait AggregateFunctionFactory[Domain <: AnyRef, AggregationValue <: Any, AggregateFunctionType <: AggregateFunction[Domain, AggregationValue]]
{
    def apply(): AggregateFunctionType

}

