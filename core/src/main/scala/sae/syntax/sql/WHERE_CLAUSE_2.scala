package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 *
 * TODO this needs operator precedence
 */
trait WHERE_CLAUSE_2[DomainA <: AnyRef, DomainB <: AnyRef, ActiveDomain <: AnyRef, Range <: AnyRef]
    extends WHERE_CLAUSE[(DomainA, DomainB), Range]
    with SQL_QUERY[Range]
{

    def AND(predicateA: ActiveDomain => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, ActiveDomain, Range]

    def AND(predicate: WHERE_CLAUSE_PREDICATE_TYPE_SWITCH[DomainB]): WHERE_CLAUSE_2[DomainA, DomainB, DomainB, Range]

    def OR(predicateA: ActiveDomain => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, ActiveDomain, Range]

    def OR(predicate: WHERE_CLAUSE_PREDICATE_TYPE_SWITCH[DomainB]): WHERE_CLAUSE_2[DomainA, DomainB, DomainB, Range]

    def AND[RangeA <: AnyRef, RangeB <: AnyRef](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR[RangeA <: AnyRef, RangeB <: AnyRef](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]


}
