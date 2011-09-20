package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:11
 *
 */

object InstanceOfKind
    extends DependencyKind
{
    val designator = "instanceof"

    val parent = Some(AllKind)

    val children = Nil
}