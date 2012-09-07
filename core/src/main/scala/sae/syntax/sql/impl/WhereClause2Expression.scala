package sae.syntax.sql.impl

import sae.syntax.sql.ast._
import predicates.{WhereClauseSequence, Filter}
import sae.syntax.sql
import sql.{WHERE_CLAUSE_EXPRESSION_2, WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereClause2Expression[DomainA <: AnyRef, DomainB <: AnyRef](conditions: Seq[WhereClauseExpression])
    extends WHERE_CLAUSE_EXPRESSION_2[DomainA, DomainB]
    with WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]
{
    def AND(predicate: (DomainA) => Boolean) =
        WhereClause2Expression (conditions ++ Seq (AndOperator, Filter (predicate, 1)))

    def OR(predicate: (DomainA) => Boolean) =
        WhereClause2Expression (conditions ++ Seq (OrOperator, Filter (predicate, 1)))

    def AND[RangeA, RangeB](join: sql.JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2Expression (conditions ++ Seq (AndOperator, join))

    def OR[RangeA, RangeB](join: sql.JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2Expression (conditions ++ Seq (OrOperator, join))

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2Expression (conditions ++ Seq (AndOperator, WhereClauseSequence (subExpression.representation)))

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2Expression (conditions ++ Seq (OrOperator, WhereClauseSequence (subExpression.representation)))

    type Representation = Seq[WhereClauseExpression]

    def representation = conditions
}
