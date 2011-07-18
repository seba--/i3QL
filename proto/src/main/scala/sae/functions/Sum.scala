package sae
package functions
import sae.operators._

/**
 * A aggregation function that calculates the sum over a set of domain entries
 * @author Malte V
 */
private class SumIntern[Domain <: AnyRef](val f : Domain => Int) extends SelfMaintainalbeAggregateFunction[Domain, Int] {
    var sum = 0
    def add(d : Domain) = {
        sum += f(d)
        sum
    }
    def remove(d : Domain) = {
        sum -= f(d)
        sum
    }

    def update(oldV : Domain, newV : Domain) = {
        sum -= f(oldV)
        sum += f(newV)
        sum
    }
}
object Sum {
    def apply[Domain <: AnyRef](f : (Domain => Int)) = {
        new SelfMaintainalbeAggregateFunctionFactory[Domain, Int] {
            def apply() : SelfMaintainalbeAggregateFunction[Domain, Int] = {
                new SumIntern[Domain](f)
            }
        }
    }
}

