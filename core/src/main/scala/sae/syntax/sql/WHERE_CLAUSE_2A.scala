package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 */

trait WHERE_CLAUSE_2A[DomainA <: AnyRef]
{

    def AND(predicate: DomainA => Boolean): WHERE_CLAUSE_2A[DomainA]

    def OR(predicate: DomainA => Boolean): WHERE_CLAUSE_2A[DomainA]

}
