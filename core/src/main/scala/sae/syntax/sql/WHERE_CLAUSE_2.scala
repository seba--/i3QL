package sae.syntax.sql


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:41
 *
 * The top level where clause has its own type since we can compile this to a query of type Range
 */
trait WHERE_CLAUSE_2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef]
    extends SQL_QUERY[Range]
{

    def AND (predicateA: DomainA => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR (predicateA: DomainA => Boolean): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def AND[RangeA, RangeB] (join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR[RangeA, RangeB] (join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

}
