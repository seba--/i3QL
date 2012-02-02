package unisson.model.kinds.operator

import unisson.model.kinds.KindExpr


/**
 *
 * Author: Ralf Mitschke
 * Date: 10.12.11
 * Time: 12:52
 *
 */
case class Union(left: KindExpr, right: KindExpr)
        extends KindExpr
{
    lazy val asVespucciString = left.asVespucciString + ", " + right.asVespucciString
}