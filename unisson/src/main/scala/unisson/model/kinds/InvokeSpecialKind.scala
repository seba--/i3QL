package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:04
 *
 */

object InvokeSpecialKind
    extends DependencyKind
{
    val designator = "invoke_special"

    val parent = Some(CallsKind)

    val children = Nil
}