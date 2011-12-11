package unisson.model.kinds.resolver

import unisson.model.kinds._
import unisson.model.kinds.operator._
import unisson.model.kinds.group._


/**
 *
 * Author: Ralf Mitschke
 * Date: 10.12.11
 * Time: 16:51
 *
 */
object KindResolver
{

    def apply(e: KindExpr): Set[DependencyKind] = e match
    {
        case Union(left, right) => apply(left) union apply(right)
        case Difference(left, right) => apply(left) diff apply(right)
        case Not(expr) => apply(AllKind) diff apply(expr)
        case group : DependencyKindGroup => group.kinds
        case kind : DependencyKind => Set(kind)
    }

}