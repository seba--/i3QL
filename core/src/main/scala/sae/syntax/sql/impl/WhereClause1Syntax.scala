package sae.syntax.sql.impl

import sae.syntax.sql.ast._
import predicates.{WhereClauseSequence, Filter}
import sae.syntax.sql.{WHERE_CLAUSE_FINAL_SUB_EXPRESSION, WHERE_CLAUSE}
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

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[Domain]) =
        WhereClause1Syntax (
            query.append (AndOperator, WhereClauseSequence (subExpression.representation))
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[Domain]) =
        WhereClause1Syntax (
            query.append (OrOperator, WhereClauseSequence (subExpression.representation))
        )


    def compile() = Compiler (query)


}
