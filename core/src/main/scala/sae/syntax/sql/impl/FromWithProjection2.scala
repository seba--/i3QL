package sae.syntax.sql.impl

import sae.LazyView
import sae.operators.{BagProjection, Conversions, CrossProduct}
import sae.syntax.sql.{STAR, INLINE_WHERE_CLAUSE, FROM_CLAUSE_2, FROM_CLAUSE}

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
    def compile() = withDistinct(
        new BagProjection[(DomainA, DomainB), Range](
            projection,
            new CrossProduct[DomainA, DomainB](
                Conversions.lazyViewToMaterializedView(relationA),
                Conversions.lazyViewToMaterializedView(relationB)
            )
        ),
        distinct
    )

    def WHERE(predicate: ((DomainA, DomainB)) => Boolean) =
        WhereWithProjection(
            projection,
            predicate,
            new CrossProduct[DomainA, DomainB](
                Conversions.lazyViewToMaterializedView(relationA),
                Conversions.lazyViewToMaterializedView(relationB)
            ),
            distinct
        )

    def WHERE(predicatesA: INLINE_WHERE_CLAUSE[DomainA], predicatesB: INLINE_WHERE_CLAUSE[DomainB]) = null

    def WHERE(predicatesA: INLINE_WHERE_CLAUSE[DomainA], predicatesB: STAR) = null

    def WHERE(predicatesA: STAR, predicatesB: INLINE_WHERE_CLAUSE[DomainB]) = null

    def WHERE(predicateA: (DomainA) => Boolean, predicateB: (DomainB) => Boolean) = null
}