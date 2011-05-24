package sae
package functions
import sae.operators._

private class SumIntern[Domain <: AnyRef](val f : Domain => Int) extends SelfMaintainalbeAggregationFunction[Domain, Int] {
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
        new SelfMaintainalbeAggregationFunctionFactory[Domain, Int] {
            def apply() : SelfMaintainalbeAggregationFunction[Domain, Int] = {
                new SumIntern[Domain](f)
            }
        }
    }
}

