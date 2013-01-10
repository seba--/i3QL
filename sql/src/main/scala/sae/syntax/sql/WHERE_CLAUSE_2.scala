package sae.syntax.sql

import ast.SQLQuery


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

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[DomainA]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[DomainA]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def AND[RangeA, RangeB] (join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def OR[RangeA, RangeB] (join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_2[DomainA, DomainB, Range]

    def AND[UnboundDomain <: AnyRef, RangeA, UnboundRange](join: JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, UnboundDomain, RangeA, UnboundRange]): WHERE_CLAUSE_2_UNBOUND_1[DomainA, UnboundDomain, Range]

    def OR[UnboundDomain <: AnyRef, RangeA, UnboundRange](join: JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, UnboundDomain, RangeA, UnboundRange]): WHERE_CLAUSE_2_UNBOUND_1[DomainA, UnboundDomain, Range]

    // TODO needs AST due to implicit conversion, not nice
    def query: SQLQuery[Range]
}
