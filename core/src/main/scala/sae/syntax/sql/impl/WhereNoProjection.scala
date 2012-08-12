package sae.syntax.sql.impl

import sae.syntax.sql.{INLINE_WHERE_CLAUSE, WHERE_CLAUSE}
import sae.operators.LazySelection
import sae.LazyView

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereNoProjection[Domain <: AnyRef](filter: Domain => Boolean,
                                               relation: LazyView[Domain],
                                               distinct: Boolean)
    extends WHERE_CLAUSE[Domain, Domain]
{
    def compile() = withDistinct (
        new LazySelection[Domain](filter, relation),
        distinct
    )

    def AND(predicate: (Domain) => Boolean) = WhereNoProjection ((x) => filter (x) && predicate (x), relation, distinct)

    def OR(predicate: (Domain) => Boolean) = WhereNoProjection ((x) => filter (x) || predicate (x), relation, distinct)

    def AND(inlineWhereClause: INLINE_WHERE_CLAUSE[Domain]) = WhereNoProjection ((x: Domain) => filter (x) && inlineWhereClause.function (x), relation, distinct)

    def OR(inlineWhereClause: INLINE_WHERE_CLAUSE[Domain]) = WhereNoProjection ((x: Domain) => filter (x) || inlineWhereClause.function (x), relation, distinct)
}
