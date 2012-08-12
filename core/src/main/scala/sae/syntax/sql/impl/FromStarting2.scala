package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql._

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 15:35
 */

case class FromStarting2[DomainA <: AnyRef, DomainB <: AnyRef](relationA: LazyView[DomainA], relationB: LazyView[DomainB])
    extends FROM_CLAUSE_AS_PREFIX_2[DomainA, DomainB]
{

    def SELECT[Range <: AnyRef](projection: (DomainA, DomainB) => Range) =
        FromWithProjection2 (
            (tuple: (DomainA, DomainB)) => projection (tuple._1, tuple._2),
            relationA,
            relationB,
            distinct = false)

    def SELECT[RangeA <: AnyRef, RangeB](projectionA: (DomainA) => RangeA, projectionB: (DomainB) => RangeB) =
        FromWithProjection2 (
            (tuple: (DomainA, DomainB)) => (projectionA (tuple._1), projectionB (tuple._2)),
            relationA,
            relationB,
            distinct = false)

    def SELECT(x: STAR_KEYWORD) =
        FromNoProjection2 (relationA, relationB, distinct = false)

    def SELECT[Range <: AnyRef](distinct: DISTINCT_INFIX_SELECT_CLAUSE[(DomainA, DomainB), Range]) =
        FromWithProjection2 (
            distinct.function,
            relationA,
            relationB,
            distinct = true)

    def SELECT(distinct: DISTINCT_INFIX_SELECT_CLAUSE_NO_PROJECTION) =
        FromNoProjection2 (relationA, relationB, distinct = true)

}
