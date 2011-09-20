package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:23
 *
 */

object SubtypeKind
extends DependencyKind
{
    val designator = "calls"

    val parent = Some(AllKind)

    val children = List(ExtendsKind, ImplementsKind)
}