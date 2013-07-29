package idb.syntax.iql

import idb.syntax.iql.IR._

/**
 * An aggregation function that calculates the sum over a set of domain entries
 *
 * @author Ralf Mitschke
 */
case object SUM
    extends AGGREGATE_FUNCTION_FACTORY[Int, Int]
{

    /*
    def added (v: Rep[Domain], previousResult: Rep[Int]) =
        previousResult + toValue (v)

    def removed (v: Rep[Domain], previousResult: Rep[Int]) =
        previousResult - toValue (v)

    def updated (oldV: Rep[Domain], newV: Rep[Domain], previousResult: Rep[Int]) =
        previousResult - toValue (oldV) + toValue (newV)

    */
}