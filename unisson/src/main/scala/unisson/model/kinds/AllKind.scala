package unisson.model.kinds

/**
 *
 * Author: Ralf Mitschke
 * Created: 12.09.11 09:52
 *
 */

object AllKind
        extends DependencyKind
{
    val designator = "all"

    val parent = None

    var children = List(
        SubtypeKind,
        CallsKind,
        SignatureKind,
        ThrowsKind,
        CreateKind,
        FieldTypeKind,
        InstanceOfKind,
        ReadFieldKind,
        WriteFieldKind,
        ThrowsKind,
        ClassCastKind
    )
}