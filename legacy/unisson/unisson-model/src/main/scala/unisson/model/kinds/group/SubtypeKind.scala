package unisson.model.kinds.group

import unisson.model.kinds.{KindExpr, DependencyKindGroup}
import unisson.model.kinds.primitive.{ImplementsKind, ExtendsKind}


/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:23
 *
 */

object SubtypeKind
extends DependencyKindGroup
with KindExpr
{
    val asVespucciString = "calls"

    val kinds = Set(ExtendsKind, ImplementsKind)
}