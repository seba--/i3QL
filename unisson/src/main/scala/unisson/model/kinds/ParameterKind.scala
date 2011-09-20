package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:23
 *
 */

object ParameterKind
extends DependencyKind
{
    val designator = "parameter"

    val parent = Some(SubtypeKind)

    val children = Nil
}