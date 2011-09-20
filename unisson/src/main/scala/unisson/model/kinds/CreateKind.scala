package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:04
 *
 */

object CreateKind
    extends DependencyKind
{
    val designator = "create"

    val parent = Some(AllKind)

    val children = Nil
}