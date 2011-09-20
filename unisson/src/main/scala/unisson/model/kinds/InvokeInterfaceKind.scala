package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:04
 *
 */

object InvokeInterfaceKind
    extends DependencyKind
{
    val designator = "invoke_interface"

    val parent = Some(CallsKind)

    val children = Nil
}