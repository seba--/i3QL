package unisson.model.kinds.group

import unisson.model.kinds.{DependencyKindGroup, KindExpr}
import unisson.model.kinds.primitive._


/**
 *
 * Author: Ralf Mitschke
 * Created: 12.09.11 09:52
 *
 */
object AllKind
        extends DependencyKindGroup
        with KindExpr
{
    val asVespucciString = "all"

    var kinds = Set(
        ClassCastKind,
        CreateKind,
        ExtendsKind,
        FieldTypeKind,
        ImplementsKind,
        InstanceOfKind,
        InvokeInterfaceKind,
        InvokeSpecialKind,
        InvokeStaticKind,
        InvokeVirtualKind,
        ParameterKind,
        ReadFieldKind,
        ReturnTypeKind,
        ThrowsKind,
        WriteFieldKind
    )
}