package idb.functions

import idb.operators.{NotSelfMaintainableAggregateFunctionFactory, NotSelfMaintainableAggregateFunction}

/**
 * A aggregation function that finds the maximum in set of domain entries
 * @author Malte V
 */
private class MaxIntern[Domain <: AnyRef](val f: Domain => Int)
        extends NotSelfMaintainableAggregateFunction[Domain, Int]
{
    var max = Integer.MIN_VALUE

    def add(d: Domain, data: Seq[Domain]) = {
        if (f(d) > max)
            max = f(d)
        max
    }

    def remove(d: Domain, data: Seq[Domain]) = {
        if (f(d) == max) {
            max = f(data.head)
            max = (max /: data)((i, s) => if (i > f(s)) i else f(s))
        }
        max
    }

    def update(oldV: Domain, newV: Domain, data: Seq[Domain]) = {
        if (f(oldV) == max || f(newV) > max) {
            max = f(data.head)
            max = (max /: data)((i, s) => if (i > f(s)) i else f(s))
        }
        max
    }

	def get = max
}

object Max
{
    def apply[Domain <: AnyRef](f: (Domain => Int)) = {
        new NotSelfMaintainableAggregateFunctionFactory[Domain, Int]
        {
            def apply(): NotSelfMaintainableAggregateFunction[Domain, Int] = {
                new MaxIntern[Domain](f)
            }
        }
    }
}