package sae.syntax.sql.impl

import sae.syntax.sql.{INLINE_WHERE_CLAUSE, WHERE_CLAUSE}
import sae.operators.{BagProjection, LazySelection}
import sae.LazyView

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereWithProjection2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: (DomainA, DomainB) => Range,
                                                                                       filter: Option[((DomainA, DomainB)) => Boolean],
                                                                                       filterA: Option[DomainA => Boolean],
                                                                                       filterB: Option[DomainB => Boolean],
                                                                                       relationA: LazyView[DomainA],
                                                                                       relationB: LazyView[DomainB],
                                                                                       distinct: Boolean = false)
    extends WHERE_CLAUSE[(DomainA, DomainB), Range]
{
    def compile() = null

    def AND(predicate: ((DomainA, DomainB)) => Boolean) = null

    def OR(predicate: ((DomainA, DomainB)) => Boolean) = null

    def AND(inlineWhereClause: INLINE_WHERE_CLAUSE[(DomainA, DomainB)]) = null

    def OR(inlineWhereClause: INLINE_WHERE_CLAUSE[(DomainA, DomainB)]) = null
}
