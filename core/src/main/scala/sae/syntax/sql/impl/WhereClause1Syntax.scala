package sae.syntax.sql.impl

import sae.syntax.sql.ast._
import predicates.Filter
import sae.syntax.sql.{WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0, WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1, WHERE_CLAUSE}
import sae.syntax.sql.compiler.Compiler

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereClause1Syntax[Domain <: AnyRef, Range <: AnyRef](query: SQLQuery[Range])
    extends WHERE_CLAUSE[Domain, Range]
{
    def AND(predicate: (Domain) => Boolean) =
        WhereClause1Syntax (
            query.append (AndOperator, Filter (predicate, 1))
        )

    def OR(predicate: (Domain) => Boolean) =
        WhereClause1Syntax (
            query.append (OrOperator, Filter (predicate, 1))
        )


    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0) =
        WhereClause1Syntax (
            query.append (AndOperator, subExpression.representation)
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_0) =
        WhereClause1Syntax (
            query.append (OrOperator, subExpression.representation)
        )

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[Domain]) =
        WhereClause1Syntax (
            query.append (AndOperator, subExpression.representation)
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[Domain]) =
        WhereClause1Syntax (
            query.append (OrOperator, subExpression.representation)
        )


    def compile() = Compiler (query)

}
