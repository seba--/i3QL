package sae.syntax.sql.impl

import sae.syntax.sql.{INLINE_WHERE_CLAUSE, WHERE_CLAUSE}
import sae.LazyView

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereNoProjection2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](filter: Option[((DomainA, DomainB)) => Boolean],
                                                                                     filterA: Option[DomainA => Boolean],
                                                                                     filterB: Option[DomainB => Boolean],
                                                                                     relationA: LazyView[DomainA],
                                                                                     relationB: LazyView[DomainB],
                                                                                     distinct: Boolean = false)
    extends WHERE_CLAUSE[(DomainA, DomainB), Range]
{
    def compile() = null

    def AND(predicate: ((DomainA, DomainB)) => Boolean) = filter match {
        case Some (f) => WhereNoProjection2 (Some ((x) => f (x) && predicate (x)), filterA, filterB, relationA, relationB, distinct)
        case None => WhereNoProjection2 (Some (predicate), filterA, filterB, relationA, relationB, distinct)
    }


    def OR(predicate: ((DomainA, DomainB)) => Boolean) = filter match {
        case Some (f) => WhereNoProjection2 (Some ((x) => f (x) || predicate (x)), filterA, filterB, relationA, relationB, distinct)
        case None => WhereNoProjection2 (Some (predicate), filterA, filterB, relationA, relationB, distinct)
    }

    def AND(inlineWhereClause: INLINE_WHERE_CLAUSE[(DomainA, DomainB)]) = null

    def OR(inlineWhereClause: INLINE_WHERE_CLAUSE[(DomainA, DomainB)]) = null
}
