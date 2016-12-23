package idb.syntax.iql

import idb.syntax.iql.IR._

/**
 * An aggregation function that calculates the sum over a set of domain entries
 *
 * @author Ralf Mitschke
 */
case object SUM
    extends AGGREGATE_FUNCTION_FACTORY_SELF_MAINTAINED[Int, Int] {
    def start: Int = 0

    def added[Domain] (v: Rep[Domain],
        previousResult: Rep[Int],
        column: Rep[Domain] => Rep[Int]
    ): Rep[Int] =
        previousResult + column (v)

    def removed[Domain] (v: Rep[Domain],
        previousResult: Rep[Int],
        column: Rep[Domain] => Rep[Int]
    ): Rep[Int] =
        previousResult - column (v)

    def updated[Domain] (oldV: Rep[Domain],
        newV: Rep[Domain],
        previousResult: Rep[Int],
        column: Rep[Domain] => Rep[Int]
    ): Rep[Int] =
        previousResult - column (oldV) + column (newV)

}