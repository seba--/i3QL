package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:03
 *
 */

object CallsKind
    extends DependencyKind
{
    val designator = "calls"

    val parent = Some(AllKind)

    val children = List(InvokeInterfaceKind, InvokeSpecialKind, InvokeStaticKind, InvokeVirtualKind)
}