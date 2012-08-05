package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql.FROM_CLAUSE
import sae.operators.{Conversions, CrossProduct}

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
private[sql] case class FromNoProjection2[DomainA <: AnyRef, DomainB <: AnyRef](relationA: LazyView[DomainA], relationB: LazyView[DomainB], distinct: Boolean)
    extends FROM_CLAUSE[(DomainA, DomainB), (DomainA, DomainB)]
{

    def compile() =
        withDistinct (
            new CrossProduct[DomainA, DomainB](
                Conversions.lazyViewToMaterializedView (relationA),
                Conversions.lazyViewToMaterializedView (relationB)
            ),
            distinct
        )

    def WHERE(predicate: ((DomainA, DomainB)) => Boolean) = null
}