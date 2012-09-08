package sae.syntax.sql.impl

import sae.syntax.sql.ast._
import predicates.Filter
import sae.syntax.sql.WHERE_CLAUSE_FINAL_SUB_EXPRESSION

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereClause1From2Syntax[Domain <: AnyRef, Range <: AnyRef](override val query: SQLQuery[Range])
    extends WhereClause1Syntax[Domain, Range](query)
{
    override def AND(predicate: (Domain) => Boolean) =
        WhereClause1From2Syntax (
            query.append (AndOperator, Filter (predicate, 2))
        )

    override def OR(predicate: (Domain) => Boolean) =
        WhereClause1From2Syntax (
            query.append (OrOperator, Filter (predicate, 2))
        )

    override def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[Domain]) =
        WhereClause1From2Syntax (
            query.append (AndOperator, Util.filtersToOtherRelation[Domain](subExpression.representation, 1, 2))
        )

    override def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION[Domain]) =
        WhereClause1From2Syntax (
            query.append (OrOperator, Util.filtersToOtherRelation[Domain](subExpression.representation, 1, 2))
        )


}
