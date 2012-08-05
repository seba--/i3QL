package sae.syntax.sql

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 21:11
 *
 */
trait STARTING_FROM_CLAUSE_2[DomainA <: AnyRef, DomainB <: AnyRef]
{

    def SELECT[Range <: AnyRef](projection: (DomainA, DomainB) => Range): FROM_CLAUSE_2[DomainA, DomainB, Range]

    def SELECT[RangeA <: AnyRef, RangeB](projectionA: DomainA => RangeA, projectionB : DomainB => RangeB): FROM_CLAUSE_2[DomainA, DomainB, (RangeA,RangeB)]

    def SELECT(x: STAR): FROM_CLAUSE_2[DomainA, DomainB, (DomainA, DomainB)]

    def SELECT[Range <: AnyRef](distinct: DISTINCT_PROJECTION[(DomainA, DomainB), Range]): FROM_CLAUSE_2[DomainA, DomainB, Range]

    def SELECT(distinct: DISTINCT_NO_PROJECTION.type): FROM_CLAUSE_2[DomainA, DomainB, (DomainA, DomainB)]

}