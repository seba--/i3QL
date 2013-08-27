package unisson.model.kinds.group

import unisson.model.kinds.{DependencyKindGroup, KindExpr}
import unisson.model.kinds.primitive.{InvokeVirtualKind, InvokeStaticKind, InvokeSpecialKind, InvokeInterfaceKind}


/**
 *
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:03
 *
 */
object CallsKind
        extends DependencyKindGroup with KindExpr
{
    val asVespucciString = "calls"

    val kinds = Set(InvokeInterfaceKind, InvokeSpecialKind, InvokeStaticKind, InvokeVirtualKind)
}