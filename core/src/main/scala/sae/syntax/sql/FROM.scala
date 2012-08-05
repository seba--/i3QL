package sae.syntax.sql

import impl.{FromStarting2, FromStarting}
import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 21:15
 *
 */
object FROM
{

    def apply[Domain <: AnyRef](relation: LazyView[Domain]): STARTING_FROM_CLAUSE[Domain] =
        FromStarting (relation)

    def apply[DomainA <: AnyRef, DomainB <: AnyRef](relationA: LazyView[DomainA], relationB: LazyView[DomainB]): STARTING_FROM_CLAUSE_MULTI_RELATION2[DomainA, DomainB] =
        FromStarting2 (relationA, relationB)

}