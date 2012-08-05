package sae.syntax.sql.impl

import sae.LazyView
import sae.operators.{BagProjection, Conversions, CrossProduct}
import sae.syntax.sql.FROM_CLAUSE

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
    extends FROM_CLAUSE[(DomainA, DomainB), Range]
{
    def compile() = withDistinct(
        new BagProjection[(DomainA, DomainB), Range](
            projection,
            new CrossProduct[DomainA, DomainB](
                Conversions.lazyViewToMaterializedView(relationA),
                Conversions.lazyViewToMaterializedView(relationB)
            )
        ),
        distinct)

    def WHERE(predicate: ((DomainA, DomainB)) => Boolean) = null
}