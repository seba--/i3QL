package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:04
 *
 */

object ReadFieldKind
    extends DependencyKind
{
    val designator = "read_field"

    val parent = Some(AllKind)

    val children = Nil
}