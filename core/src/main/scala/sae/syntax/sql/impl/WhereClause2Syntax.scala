package sae.syntax.sql.impl

import sae.syntax.sql.ast._
import predicates.{WhereClauseSequence, Filter}
import sae.syntax.sql
import sql.compiler.Compiler
import sql.{WHERE_CLAUSE_FINAL_SUB_EXPRESSION, WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2, WHERE_CLAUSE_2}

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

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2Syntax (
            query.append (AndOperator, WhereClauseSequence (subExpression.representation))
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2Syntax (
            query.append (OrOperator, WhereClauseSequence (subExpression.representation))
        )

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[DomainB]) =
        WhereClause1From2Syntax (
            if (subExpression.representation.head.isInstanceOf[Filter[DomainB]])
            {
                // special case, if we started with a filter we need to map all filters to the second relation
                query.append (AndOperator, WhereClauseSequence (Util.mapFiltersTo2[DomainB](subExpression.representation)))
            }
            else
            {
                // in this case we have started out with a join or else the DomainA, and ended at DomainB, hence the filters all apply to correct relations
                query.append (AndOperator, WhereClauseSequence (subExpression.representation))
            }
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[DomainB]) =
        WhereClause1From2Syntax (
            if (subExpression.representation.head.isInstanceOf[Filter[DomainB]])
            {
                // special case, if we started with a filter we need to map all filters to the second relation
                query.append (OrOperator, WhereClauseSequence (Util.mapFiltersTo2[DomainB](subExpression.representation)))
            }
            else
            {
                // in this case we have started out with a join or else the DomainA, and ended at DomainB, hence the filters all apply to correct relations
                query.append (OrOperator, WhereClauseSequence (subExpression.representation))
            }
        )

    def compile() = Compiler (query)


}
