package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql.SELECT_CLAUSE_2

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:55
 *
 */
case class Projection2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: (DomainA, DomainB) => Range, distinct: Boolean)
    extends SELECT_CLAUSE_2[DomainA, DomainB, Range]
{
    def FROM(relationA: LazyView[DomainA], relationB: LazyView[DomainB]) =
        FromWithProjection2 ((tuple: (DomainA, DomainB)) => projection (tuple._1, tuple._2), relationA, relationB, distinct)

}