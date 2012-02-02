package sae.operators

import sae.operators.intern._

/**
 * Factory interface for a not self maintainable aggregation function
 */
trait NotSelfMaintainableAggregateFunctionFactory[Domain <: AnyRef, AggregateValue <: Any]
        extends AggregateFunctionFactory[Domain, AggregateValue, NotSelfMaintainableAggregateFunction[Domain, AggregateValue]]
{

}

/**
 * Factory interface for a self maintainable aggregation function
 */
trait SelfMaintainableAggregateFunctionFactory[Domain <: AnyRef, AggregateValue <: Any]
        extends AggregateFunctionFactory[Domain, AggregateValue, SelfMaintainableAggregateFunction[Domain, AggregateValue]]
{

}
