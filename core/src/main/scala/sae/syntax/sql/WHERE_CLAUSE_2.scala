package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 *
 */
trait WHERE_CLAUSE_2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef]
    extends SQL_QUERY[Range]
{

    def AND(predicateA: DomainA => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR(predicateA: DomainA => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    //def AND(predicate: WHERE_CLAUSE_PREDICATE_TYPE_SWITCH[DomainB]): WHERE_CLAUSE[DomainB, Range]

    //def OR[Other <: AnyRef](predicate: WHERE_CLAUSE_PREDICATE_TYPE_SWITCH[Other]): WHERE_CLAUSE[Other, Range]

    def AND[RangeA <: AnyRef, RangeB <: AnyRef](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR[RangeA <: AnyRef, RangeB <: AnyRef](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]


}
