package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:23
 *
 */

object ImplementsKind
extends DependencyKind
{
    val designator = "implements"

    val parent = Some(SubtypeKind)

    val children = Nil
}