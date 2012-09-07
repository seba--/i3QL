package sae.syntax.sql.impl

import sae.syntax.sql.ast._
import predicates.Filter
import sae.syntax.sql
import sql.{WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2, WHERE_CLAUSE_2}

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
            query.append (AndOperator, Filter (predicateA))
        )

    def OR(predicateA: (DomainA) => Boolean) =
        WhereClause2Syntax (
            query.append (OrOperator, Filter (predicateA))
        )

    def AND[RangeA, RangeB](join: sql.JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2Syntax (
            query.append (AndOperator, join)
        )

    def OR[RangeA, RangeB](join: sql.JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2Syntax (
            query.append (OrOperator, join)
        )

    //def AND[SuperDomainA >: DomainA <: AnyRef, SuperDomainB >: DomainB  <: AnyRef](subExpression: WHERE_CLAUSE_EXPRESSION_2[SuperDomainA, SuperDomainB]) =
    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2Syntax (
            query.append (AndOperator, subExpression)
        )

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]) =
        WhereClause2Syntax (
            query.append (OrOperator, subExpression)
        )

    def compile() = Compiler (query)


}
