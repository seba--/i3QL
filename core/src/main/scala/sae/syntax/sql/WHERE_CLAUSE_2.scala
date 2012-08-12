package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 *
 * TODO this needs operator precedence
 */
trait WHERE_CLAUSE_2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef]
    extends WHERE_CLAUSE[(DomainA, DomainB), Range]
    with SQL_QUERY[Range]
{

    def AND(predicateA: DomainA => Boolean, predicateB: DomainB => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR(predicateA: DomainA => Boolean, predicateB: DomainB => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def AND[RangeA, RangeB](join: (DomainA => RangeA, DomainB => RangeB)): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR[RangeA, RangeB](join: (DomainA => RangeA, DomainB => RangeB)): WHERE_CLAUSE_2[DomainA, DomainB, Range]


}
