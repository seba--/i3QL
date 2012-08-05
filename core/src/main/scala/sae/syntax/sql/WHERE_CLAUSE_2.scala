package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 */

trait WHERE_CLAUSE_2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef]
    extends SQL_END_CLAUSE[Range]
{

    def AND(predicate: DomainA => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR(predicate: DomainB => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

}
