package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:04
 *
 */

object InvokeStaticKind
    extends DependencyKind
{
    val designator = "invoke_static"

    val parent = Some(CallsKind)

    val children = Nil
}