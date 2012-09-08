package sae.syntax.sql.impl

import sae.syntax.sql.{WHERE_CLAUSE_EXPRESSION, WHERE_CLAUSE_FINAL_SUB_EXPRESSION}
import sae.syntax.sql.ast._
import predicates.{WhereClauseSequence, Filter}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 20:41
 *
 */
case class WhereClause1Expression[Domain <: AnyRef](conditions: Seq[WhereClauseExpression])
    extends WHERE_CLAUSE_EXPRESSION[Domain]
    with WHERE_CLAUSE_FINAL_SUB_EXPRESSION[Domain]
{
    def AND(predicate: (Domain) => Boolean) =
        WhereClause1Expression (conditions ++ Seq (AndOperator, Filter (predicate, 1)))

    def OR(predicate: (Domain) => Boolean) =
        WhereClause1Expression (conditions ++ Seq (OrOperator, Filter (predicate, 1)))


    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[Domain]) =
        WhereClause1Expression (conditions ++ Seq (AndOperator, subExpression.representation))

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[Domain]) =
        WhereClause1Expression (conditions ++ Seq (OrOperator, subExpression.representation))

    type Representation = WhereClauseSequence

    def representation = WhereClauseSequence(conditions)
}
