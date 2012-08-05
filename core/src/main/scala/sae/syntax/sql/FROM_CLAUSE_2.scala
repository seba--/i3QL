package sae.syntax.sql


/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:51
 *
 */
trait FROM_CLAUSE_2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef]
    extends SQL_END_CLAUSE[Range]
{

    def WHERE(predicate: DomainA => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def WHERE(predicate: DomainB => Boolean): WHERE_CLAUSE_2[DomainB, DomainB, Range]

}