package sae.syntax.sql.impl

import sae.syntax.sql.WHERE_CLAUSE
import sae.operators.{BagProjection, LazySelection}
import sae.LazyView

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereWithProjection[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range,
                                                              filter: Domain => Boolean,
                                                              relation: LazyView[Domain],
                                                              distinct: Boolean = false)
    extends WHERE_CLAUSE[Domain, Range]
{
    def compile() = withDistinct (
        new BagProjection[Domain, Range](projection, new LazySelection[Domain](filter, relation)),
        distinct
    )

}
