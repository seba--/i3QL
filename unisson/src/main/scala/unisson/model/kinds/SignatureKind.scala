package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:23
 *
 */

object SignatureKind
extends DependencyKind
{
    val designator = "signature"

    val parent = Some(SubtypeKind)

    val children = List(ParameterKind, ReturnTypeKind)
}