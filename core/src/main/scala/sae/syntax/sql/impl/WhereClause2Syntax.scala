package sae.syntax.sql.impl

import sae.syntax.sql.ast._
import predicates.{WhereClauseSequence, Filter}
import sae.syntax.sql
import sql.compiler.Compiler
import sql.{WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0, WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1, WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2, WHERE_CLAUSE_2}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereClause2Syntax[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](query: SQLQuery[Range])
    extends WHERE_CLAUSE_2[DomainA, DomainB, Range]
{
    def AND(predicateA: (DomainA) => Boolean) =
        WhereClause2Syntax (
            query.append (AndOperator, Filter (predicateA, 1))
        )

    def OR(predicateA: (DomainA) => Boolean) =
        WhereClause2Syntax (
            query.append (OrOperator, Filter (predicateA, 1))
        )

    def AND[RangeA, RangeB](join: sql.JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2Syntax (
            query.append (AndOperator, join)
        )

    def OR[RangeA, RangeB](join: sql.JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2Syntax (
            query.append (OrOperator, join)
        )

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0) =
        WhereClause2Syntax (
            query.append (AndOperator, subExpression.representation)
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0) =
        WhereClause2Syntax (
            query.append (OrOperator, subExpression.representation)
        )

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2Syntax (
            query.append (AndOperator, subExpression.representation)
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2Syntax (
            query.append (OrOperator, subExpression.representation)
        )

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[DomainB]) = {
        val newSubExpression = subExpression.representation match {
            // special case, if we started with a filter we need to map all filters to the second relation
            case WhereClauseSequence (seq) if seq.head.isInstanceOf[Filter[DomainB]] => Util.filtersToOtherRelation[DomainB](subExpression.representation, 1, 2)
            case x => x
        }
        WhereClause1From2Syntax (
            query.append (AndOperator, newSubExpression)
        )
    }

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[DomainB]) = {
        val newSubExpression = subExpression.representation match {
            // special case, if we started with a filter we need to map all filters to the second relation
            case WhereClauseSequence (seq) if seq.head.isInstanceOf[Filter[DomainB]] => Util.filtersToOtherRelation[DomainB](subExpression.representation, 1, 2)
            case x => x
        }
        WhereClause1From2Syntax (
            query.append (OrOperator, newSubExpression)
        )
    }

    def compile() = Compiler (query)

    type Representation =  SQLQuery[Range]

    def representation = query
}
