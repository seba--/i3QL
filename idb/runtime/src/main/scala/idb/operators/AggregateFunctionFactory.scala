package idb.operators

/**
 * IMPORTANT: clients should NOT implement this interface
 * clients should implement:
 * -NotSelfMaintainableAggregateFunctionFactory
 * -SelfMaintainableAggregateFunctionFactory
 *
 */
trait AggregateFunctionFactory[Domain, AggregationValue, +AggregateFunctionType <: AggregateFunction[Domain, AggregationValue]]
{
    def apply(): AggregateFunctionType

}

/**
 * Factory interface for a not self maintainable aggregation function
 */
trait NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]
    extends AggregateFunctionFactory[Domain, AggregateValue, NotSelfMaintainableAggregateFunction[Domain, AggregateValue]]
{

}

/**
 * Factory interface for a self maintainable aggregation function
 */
trait SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]
    extends AggregateFunctionFactory[Domain, AggregateValue, SelfMaintainableAggregateFunction[Domain, AggregateValue]]
{

}
