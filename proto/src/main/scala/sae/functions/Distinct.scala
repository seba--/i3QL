package sae.functions

import sae.operators._

/**
 * distinct operator
 * @deprecated use DuplicateElimination instead
 */
@Deprecated
object Distinct {
  def apply[Domain <: AnyRef, AggregationValue <: Any, DistinctAttribut <: AnyRef]
  (aggFunc: SelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue], distincFunction: Domain => DistinctAttribut): DistinctSelfMaintainableAggregationFunctionFactory[Domain, AggregationValue] = {
    new DistinctSelfMaintainableAggregationFunctionFactory[Domain, AggregationValue] {
      def apply(): DistinctSelfMaintainableAggregationFunction[Domain, AggregationValue] = {
        new DistinctSelfMaintainableAggregationFunction[Domain, AggregationValue] {

          import com.google.common.collect._;
          val data = HashMultiset.create[DistinctAttribut]()
          var oldRes: Option[AggregationValue] = None
          val func = aggFunc()

          def add(newD: Domain) = {
            if (!data.contains(distincFunction(newD))) {
              oldRes = Some(func.add(newD))
            }
            data.add(distincFunction(newD))
            oldRes match {
              case Some(x) => x
              case _ => throw new Error
            }
          }

          def remove(newD: Domain) = {
            data.remove(distincFunction(newD))
            if (!data.contains(distincFunction(newD))) {
              oldRes = Some(func.remove(newD))
            }
            oldRes match {
              case Some(x) => x
              case _ => throw new Error
            }
          }

          def update(oldD: Domain, newD: Domain) = {
            data.remove(distincFunction(oldD))

            if (!data.contains(distincFunction(newD)) && !data.contains(distincFunction(oldD))) {
              oldRes = Some(func.update(oldD, newD))
            } else if (!data.contains(newD)) {
              oldRes = Some(func.add(newD))
            } else if (!data.contains(oldD)) {
              oldRes = Some(func.remove(newD))
            }
            data.add(distincFunction(newD))
            oldRes match {
              case Some(x) => x
              case _ => throw new Error
            }
          }
        }
      }
    }
  }

  def apply[Domain <: AnyRef, AggregationValue <: Any, DistinctAttribut <: AnyRef]
  (aggFunc: NotSelfMaintainalbeAggregationFunctionFactory[Domain, AggregationValue], distincFunction: Domain => DistinctAttribut): DistinctNotSelfMaintainableAggregationFunctionFactory[Domain, AggregationValue] = {
    new DistinctNotSelfMaintainableAggregationFunctionFactory[Domain, AggregationValue] {
      def apply(): DistinctNotSelfMaintainableAggregationFunction[Domain, AggregationValue] = {
        new DistinctNotSelfMaintainableAggregationFunction[Domain, AggregationValue] {

          import com.google.common.collect._;
          val dataIntern = HashMultiset.create[DistinctAttribut]()
          var oldRes: Option[AggregationValue] = None
          val func = aggFunc()

          override def add(newD: Domain, data: Iterable[Domain]) = {
            if (!dataIntern.contains(distincFunction(newD))) {
              oldRes = Some(func.add(newD, data))
            }
            dataIntern.add(distincFunction(newD))
            oldRes match {
              case Some(x) => x
              case _ => throw new Error
            }
          }

          override def remove(newD: Domain, data: Iterable[Domain]) = {
            dataIntern.remove(distincFunction(newD))
            if (!dataIntern.contains(distincFunction(newD))) {
              oldRes = Some(func.remove(newD, data))
            }
            oldRes match {
              case Some(x) => x
              case _ => throw new Error
            }
          }

          override def update(oldD: Domain, newD: Domain, data: Iterable[Domain]) = {
            dataIntern.remove(distincFunction(oldD))

            if (!dataIntern.contains(distincFunction(newD)) && !dataIntern.contains(distincFunction(oldD))) {
              oldRes = Some(func.update(oldD, newD, data))
            } else if (!dataIntern.contains(newD)) {
              oldRes = Some(func.add(newD, data))
            } else if (!dataIntern.contains(oldD)) {
              oldRes = Some(func.remove(newD, data))
            }
            dataIntern.add(distincFunction(newD))
            oldRes match {
              case Some(x) => x
              case _ => throw new Error
            }
          }
        }
      }
    }
  }
}