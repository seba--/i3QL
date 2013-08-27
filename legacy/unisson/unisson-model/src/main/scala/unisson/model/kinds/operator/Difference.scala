package unisson.model.kinds.operator

import unisson.model.kinds.KindExpr


/**
 *
 * Author: Ralf Mitschke
 * Date: 10.12.11
 * Time: 12:49
 *
 */
case class Difference(left: KindExpr, right: KindExpr)
        extends KindExpr
{
    lazy val asVespucciString = left.asVespucciString + "\\" + right.asVespucciString
}