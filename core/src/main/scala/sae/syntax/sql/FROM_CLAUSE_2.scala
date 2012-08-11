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

    //def WHERE(predicatesA: INLINE_WHERE_CLAUSE[DomainA], predicatesB: INLINE_WHERE_CLAUSE[DomainB]): WHERE_CLAUSE[(DomainA, DomainB), Range]

    //def WHERE(predicatesA: INLINE_WHERE_CLAUSE[DomainA], predicatesB: STAR): WHERE_CLAUSE[(DomainA, DomainB), Range]

    //def WHERE(predicatesA: STAR, predicatesB: INLINE_WHERE_CLAUSE[DomainB]): WHERE_CLAUSE[(DomainA, DomainB), Range]

    def WHERE(predicateA: DomainA => Boolean, predicateB: DomainB => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

}