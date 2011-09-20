package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:04
 *
 */

object ReturnTypeKind
    extends DependencyKind
{
    val designator = "return_type"

    val parent = Some(AllKind)

    val children = Nil
}