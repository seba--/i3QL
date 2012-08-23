package sae.syntax.sql.impl

import sae.LazyView
import sae.operators.{BagProjection, Conversions, CrossProduct}
import sae.syntax.sql._

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromWithProjection2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](
                                                                                         projection: ((DomainA, DomainB)) => Range,
                                                                                         relationA: LazyView[DomainA],
                                                                                         relationB: LazyView[DomainB],
                                                                                         distinct: Boolean
                                                                                         )
    extends FROM_CLAUSE_2[DomainA, DomainB, Range]
{
    def compile() = withDistinct (
        new BagProjection[(DomainA, DomainB), Range](
            projection,
            new CrossProduct[DomainA, DomainB](
                Conversions.lazyViewToMaterializedView (relationA),
                Conversions.lazyViewToMaterializedView (relationB)
            )
        ),
        distinct
    )

    def WHERE(predicate: ((DomainA, DomainB)) => Boolean) = null
    /*
        WhereWithProjection (
            projection,
            predicate,
            new CrossProduct[DomainA, DomainB](
                Conversions.lazyViewToMaterializedView (relationA),
                Conversions.lazyViewToMaterializedView (relationB)
            ),
            distinct
        )
    */

    def WHERE(predicatesA: INLINE_WHERE_CLAUSE[DomainA], predicatesB: INLINE_WHERE_CLAUSE[DomainB]) = null

    def WHERE(predicatesA: INLINE_WHERE_CLAUSE[DomainA], predicatesB: STAR_KEYWORD) = null

    def WHERE(predicatesA: STAR_KEYWORD, predicatesB: INLINE_WHERE_CLAUSE[DomainB]) = null

    def WHERE(predicateA: (DomainA) => Boolean, predicateB: (DomainB) => Boolean) = null

    def WHERE[RangeA <: AnyRef, RangeB <: AnyRef](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) = null

    def WHERE[SubDomain <: AnyRef, SubRange <: AnyRef](subQuery: SQL_SUB_QUERY_WHERE_OPEN_1[SubDomain, SubRange, (DomainA, DomainB)] with EXISTS_KEYWORD) {}

    def WHERE[UnboundDomain <: AnyRef, RangeA <: AnyRef, UnboundRange <: AnyRef](join: JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, UnboundDomain, RangeA, UnboundRange]) = null

}