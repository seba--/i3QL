package idb.syntax.iql

import idb.syntax.iql.IR._

/**
 * An aggregation function that calculates the arithmetic mean over a set of domain entries
 *
 * @author Mirko Köhler
 */
case object AVG
    extends AGGREGATE_FUNCTION_FACTORY_SELF_MAINTAINED[Double, Double] {

    private var currentSum : Rep[Double] = 0.0
    private var currentCount : Rep[Int] = 0

    override def start: Double = 0.0

    override def added[Domain](v: Rep[Domain], previousResult: Rep[Double], column: (Rep[Domain]) => Rep[Double]): Rep[Double] = {
        currentSum = currentSum + column(v)
        currentCount = currentCount + 1
        currentSum / currentCount
    }

    override def removed[Domain](v: Rep[Domain], previousResult: Rep[Double], column: (Rep[Domain]) => Rep[Double]): Rep[Double] = {
        currentSum = currentSum - column(v)
        currentCount = currentCount - 1
        currentSum / currentCount
    }

    override def updated[Domain](oldV: Rep[Domain], newV: Rep[Domain], previousResult: Rep[Double], column: (Rep[Domain]) => Rep[Double]): Rep[Double] = {
        currentSum = currentSum - column(oldV) + column(newV)
        currentSum / currentCount
    }
}