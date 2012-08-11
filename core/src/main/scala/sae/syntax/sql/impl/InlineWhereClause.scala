package sae.syntax.sql.impl

import sae.syntax.sql.INLINE_WHERE_CLAUSE

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 20:41
 *
 * TODO this needs operator precedence
 */
case class InlineWhereClause[Domain <: AnyRef](f : Domain => Boolean)
    extends INLINE_WHERE_CLAUSE[Domain]
{
    def AND(predicate: (Domain) => Boolean) = InlineWhereClause((x) => f(x) && predicate(x))

    def OR(predicate: (Domain) => Boolean) = InlineWhereClause((x) => f(x) || predicate(x))

    def function = f
}
