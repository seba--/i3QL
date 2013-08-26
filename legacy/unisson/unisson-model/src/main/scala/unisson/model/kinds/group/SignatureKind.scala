package unisson.model.kinds.group

import unisson.model.kinds.{KindExpr, DependencyKindGroup}
import unisson.model.kinds.primitive.{ReturnTypeKind, ParameterKind}


/**
 *
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:23
 *
 */

object SignatureKind
        extends DependencyKindGroup
        with KindExpr
{
    val asVespucciString = "signature"

    val kinds = Set(ParameterKind, ReturnTypeKind)
}