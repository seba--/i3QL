package sae.syntax.sql


/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:51
 *
 */
trait FROM_CLAUSE_2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef]
    extends FROM_CLAUSE[(DomainA, DomainB), Range]
{

    def WHERE(predicatesA: WHERE_CLAUSE_2A[DomainA], predicatesB: WHERE_CLAUSE_2B[DomainB]): WHERE_CLAUSE[(DomainA, DomainB), Range]


}