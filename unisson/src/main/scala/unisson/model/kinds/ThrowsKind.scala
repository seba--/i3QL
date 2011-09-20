package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:11
 *
 */

object ThrowsKind
    extends DependencyKind
{
    val designator = "throws"

    val parent = Some(AllKind)

    val children = Nil
}