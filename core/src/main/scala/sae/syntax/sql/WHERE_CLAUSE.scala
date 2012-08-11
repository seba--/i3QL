package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 *
 * TODO this needs operator precedence
 */
trait WHERE_CLAUSE[Domain <: AnyRef, Range <: AnyRef]
    extends SQL_END_CLAUSE[Range]
{

    implicit val ctx = this

    def AND(predicate: Domain => Boolean): WHERE_CLAUSE[Domain, Range]

    //def AND(inlineWhereClause : INLINE_WHERE_CLAUSE[Domain]): WHERE_CLAUSE[Domain, Range]

    def OR(predicate: Domain => Boolean): WHERE_CLAUSE[Domain, Range]

    //def OR(inlineWhereClause : INLINE_WHERE_CLAUSE[Domain]): WHERE_CLAUSE[Domain, Range]
}
