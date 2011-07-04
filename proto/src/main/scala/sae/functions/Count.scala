package sae.functions
import sae.operators._


/**
 * A aggregation function that counts all domain entries
 * @author Malte V
 */
private class CountIntern[Domain <: AnyRef]() extends SelfMaintainalbeAggregationFunction[Domain, Int] {
    var count = 0
    def add(d : Domain) = {
        count += 1
        count
    }
    def remove(d : Domain) = {
        count -= 1
        count
    }

    def update(oldV : Domain, newV : Domain) = {
        count
    }
}

object Count {
    def apply[Domain <: AnyRef]() : SelfMaintainalbeAggregationFunctionFactory[Domain, Int] = {
        new SelfMaintainalbeAggregationFunctionFactory[Domain, Int] {
            def apply() : SelfMaintainalbeAggregationFunction[Domain, Int] = {
                new CountIntern[Domain]()
            }
        }
    }
}