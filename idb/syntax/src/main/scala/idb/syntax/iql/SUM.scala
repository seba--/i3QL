package idb.syntax.iql

import idb.syntax.iql.IR._

/**
 * An aggregation function that calculates the sum over a set of domain entries
 *
 * @author Ralf Mitschke
 */
case class SUM[Domain] (toValue: Rep[Domain] => Rep[Int])
    extends AGGREGATE_FUNCTION_SELF[Domain, Int]
{
    def added (v: Rep[Domain], previousResult: Rep[Int]) =
        previousResult + toValue (v)

    def removed (v: Rep[Domain], previousResult: Rep[Int]) =
        previousResult - toValue (v)

    def updated (oldV: Rep[Domain], newV: Rep[Domain], previousResult: Rep[Int]) =
        previousResult - toValue (oldV) + toValue (newV)
}