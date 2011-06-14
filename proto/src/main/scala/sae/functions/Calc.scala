package sae.functions

import sae._
import sae.operators._

private class CalcIntern[Domain <: AnyRef, Result <: AnyVal](val f : Domain => Result) extends SelfMaintainalbeAggregationFunction[Domain, Option[Result]] {

    def add(d : Domain) = {
        Some(f(d))

    }
    def remove(d : Domain) = {
        None
    }

    def update(oldV : Domain, newV : Domain) = {
        Some(f(newV))
    }
}
object CalcSelfMaintable {
    def apply[Domain <: AnyRef, Result <: AnyVal](f : (Domain => Result)) = {
        new SelfMaintainalbeAggregationFunctionFactory[Domain, Option[Result]] {
            def apply() : SelfMaintainalbeAggregationFunction[Domain, Option[Result]] = {
                new CalcIntern[Domain, Result](f)
            }
        }
    }
}

