package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:11
 *
 */

object ClassCastKind
    extends DependencyKind
{
    val designator = "class_cast"

    val parent = Some(AllKind)

    val children = Nil
}