package sae.syntax.sql.impl

import sae.syntax.sql.ast._
import sae.syntax.sql.ast.WhereClause1
import sae.syntax.sql.ast.Filter
import sae.syntax.sql.{WHERE_CLAUSE_FINAL_SUB_EXPRESSION, WHERE_CLAUSE}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereClause1Syntax[Domain <: AnyRef, Range <: AnyRef](whereClause: WhereClause1[Domain, Range])
    extends WHERE_CLAUSE[Domain, Range]
{
    def AND(predicate: (Domain) => Boolean) =
        WhereClause1Syntax (
            WhereClause1 (
                whereClause.fromClause,
                whereClause.conditions ++ Seq (AndOperator, Filter (predicate))
            )
        )

    def OR(predicate: (Domain) => Boolean) =
        WhereClause1Syntax (
            WhereClause1 (
                whereClause.fromClause,
                whereClause.conditions ++ Seq (OrOperator, Filter (predicate))
            )
        )

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[Domain]) =
        WhereClause1Syntax (
            WhereClause1 (
                whereClause.fromClause,
                whereClause.conditions ++ Seq (AndOperator, subExpression)
            )
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[Domain]) =
        WhereClause1Syntax (
            WhereClause1 (
                whereClause.fromClause,
                whereClause.conditions ++ Seq (OrOperator, subExpression)
            )
        )


    def compile() = Compiler (whereClause)


}
