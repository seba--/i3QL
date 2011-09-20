package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:04
 *
 */

object InvokeVirtualKind
    extends DependencyKind
{
    val designator = "invoke_virtual"

    val parent = Some(CallsKind)

    val children = Nil
}