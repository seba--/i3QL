package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 */

trait WHERE_CLAUSE_2B[DomainB <: AnyRef]
{

    def AND(predicate: DomainB => Boolean): WHERE_CLAUSE_2B[DomainB]

    def OR(predicate: DomainB => Boolean): WHERE_CLAUSE_2B[DomainB]

}
