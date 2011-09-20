package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:04
 *
 */

object WriteFieldKind
    extends DependencyKind
{
    val designator = "write_field"

    val parent = Some(AllKind)

    val children = Nil
}