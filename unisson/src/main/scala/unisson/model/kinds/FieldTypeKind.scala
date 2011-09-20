package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:11
 *
 */

object FieldTypeKind
    extends DependencyKind
{
    val designator = "field_type"

    val parent = Some(AllKind)

    val children = Nil
}