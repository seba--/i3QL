package sae.functions
import sae.operators._

private class AVGIntern[Domain <: AnyRef](val f : Domain => Double) extends SelfMaintainalbeAggregationFunction[Domain, Double] {
    var count = 0
    var sum = 0.0
    def add(d : Domain) = {
        count += 1
        sum += f(d) 
        (sum / count)
    }
    def remove(d : Domain) = {
        count -= 1
        sum -= f(d)
        (sum / count)
    }

    def update(oldV : Domain, newV : Domain) = {
        sum -= f(oldV)
        sum += f(newV)
        (sum / count)
    }
}

object AVG {
    def apply[Domain <: AnyRef](f : (Domain => Double)) = {
        new SelfMaintainalbeAggregationFunctionFactory[Domain, Double] {
            def apply() : SelfMaintainalbeAggregationFunction[Domain, Double] = {
                new AVGIntern[Domain](f)
            }
        }
    }
}