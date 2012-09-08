package sae.syntax.sql.impl

import sae.syntax.sql.ast._
import predicates._
import predicates.Filter
import predicates.WhereClauseSequence
import sae.syntax.sql
import sql._
import ast.SQLQuery

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereClause2From1Syntax[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](query: SQLQuery[Range])
    extends WHERE_CLAUSE_2_UNBOUND_1[DomainA, DomainB, Range]
{
    def AND(predicateA: (DomainA) => Boolean) =
        WhereClause2From1Syntax (
            query.append (AndOperator, Filter (predicateA, 1))
        )

    def OR(predicateA: (DomainA) => Boolean) =
        WhereClause2From1Syntax (
            query.append (OrOperator, Filter (predicateA, 1))
        )

    def AND[RangeA, RangeB](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2From1Syntax (
            query.append (AndOperator, UnboundJoin (join.asInstanceOf[Join[DomainA, DomainB, RangeA, RangeB]])) // TODO ugly typecast
        )

    def OR[RangeA, RangeB](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2From1Syntax (
            query.append (OrOperator, UnboundJoin (join.asInstanceOf[Join[DomainA, DomainB, RangeA, RangeB]])) // TODO ugly typecast
        )

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0) =
        WhereClause2From1Syntax (
            query.append (AndOperator, subExpression.representation)
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0) =
        WhereClause2From1Syntax (
            query.append (OrOperator, subExpression.representation)
        )

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2From1Syntax (
            query.append (AndOperator, subExpression.representation)
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2From1Syntax (
            query.append (OrOperator, subExpression.representation)
        )

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[DomainA]) =
        WhereClause2From1Syntax (
            query.append (AndOperator, subExpression.representation)
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[DomainA]) =
        WhereClause2From1Syntax (
            query.append (OrOperator, subExpression.representation)
        )

    def compile() = throw new UnsupportedOperationException ("Trying to compile a subquery with open joins to outer query")

    type Representation = SQLQuery[Range]

    def representation = query




}
