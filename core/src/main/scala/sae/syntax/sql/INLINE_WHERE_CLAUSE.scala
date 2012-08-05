package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 */

trait INLINE_WHERE_CLAUSE[Domain <: AnyRef]
{

    def AND(predicate: Domain => Boolean): INLINE_WHERE_CLAUSE[Domain]

    def OR(predicate: Domain => Boolean): INLINE_WHERE_CLAUSE[Domain]

}
