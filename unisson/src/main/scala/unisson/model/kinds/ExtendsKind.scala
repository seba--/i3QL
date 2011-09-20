package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:23
 *
 */

object ExtendsKind
extends DependencyKind
{
    val designator = "extends"

    val parent = Some(SubtypeKind)

    val children = Nil
}