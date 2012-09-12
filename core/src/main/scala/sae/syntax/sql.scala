package sae.syntax

import sae.LazyView
import sae.collections.QueryResult
import sql.ast.predicates._
import sql.ast.predicates.Filter
import sql.ast.predicates.Join
import sql.ast.predicates.UnboundJoin
import sql.impl._
import sql.impl.WhereClause1Expression
import sql.impl.WhereClause1Expression
import sql.impl.WhereClause1From2Syntax
import sql.impl.WhereClause1From2Syntax
import sql.impl.WhereClause2Expression
import sql.impl.WhereClause2Expression
import sql.impl.WhereClauseComparator
import sql.impl.WhereClauseComparator

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:56
 *
 */
package object sql
{
    val * : STAR_KEYWORD = keywords.STAR_KEYWORD

    implicit def compile[Domain <: AnyRef](clause: SQL_QUERY[Domain]): LazyView[Domain] =
        clause.compile ()

    implicit def lazyViewToResult[V <: AnyRef](lazyView: LazyView[V]): QueryResult[V] = sae.collections.Conversions
        .lazyViewToResult (
        lazyView
    )

    implicit def whereClaus2ToNextDomain[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](whereClause2: WHERE_CLAUSE_2[DomainA, DomainB, Range]): WHERE_CLAUSE[DomainB, Range] =
        WhereClause1From2Syntax (whereClause2.query)

    implicit def whereClausUnbound2ToNextDomain[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](whereClause2: WHERE_CLAUSE_2_UNBOUND_1[DomainA, DomainB, Range]): WHERE_CLAUSE[DomainB, Range] =
        WhereClause1From2Syntax (whereClause2.query)


    implicit def functionToComparator[Domain <: AnyRef, Range](left: Domain => Range): WHERE_CLAUSE_COMPARATOR[Domain, Range] =
        WhereClauseComparator (left)

    implicit def functionToInClauseUnnesting[Domain <: AnyRef, Range <: AnyRef](fun: Domain => Seq[Range]): IN_CLAUSE_UNNESTING[Domain, Range] =
        InClauseUnnestingSyntax (fun)

    implicit def functionTuples2[Domain, R1, R2](functionTuple: (Domain => R1, Domain => R2)): Domain => (R1, R2) =
        (x: Domain) => (functionTuple._1 (x), functionTuple._2 (x))

    implicit def predicateToInlineWhereClause[Domain <: AnyRef](f: Domain => Boolean): WHERE_CLAUSE_EXPRESSION[Domain] =
        WhereClause1Expression (Seq (Filter (f, 1)))

    implicit def joinToInlineWhereClause[DomainA <: AnyRef, DomainB <: AnyRef, RangeA, RangeB](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_EXPRESSION_2[DomainA, DomainB] =
        WhereClause2Expression (Seq (join))

    implicit def whereClauseExpression1ToFinalSubExpression1[Domain <: AnyRef](expression: WHERE_CLAUSE_EXPRESSION[Domain]): WHERE_CLAUSE_FINAL_SUB_EXPRESSION_1[Domain] =
        WhereClause1Expression (expression.representation)

    implicit def whereClauseExpression1ToFinalSubExpression2[Domain <: AnyRef](expression: WHERE_CLAUSE_EXPRESSION[Domain]): WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[AnyRef, Domain] =
        WhereClause2Expression (Util.sequenceFiltersToOtherRelation(expression.representation, 1, 2))

    implicit def whereClauseExpression2ToFinalSubExpression2[DomainA <: AnyRef, DomainB <: AnyRef](expression: WHERE_CLAUSE_EXPRESSION_2[DomainA, DomainB]): WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB] =
        WhereClause2Expression (expression.representation)

    implicit def joinToUnboundJoin[DomainA <: AnyRef, DomainB <: AnyRef, RangeA, RangeB](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, DomainB, RangeA, RangeB] =
        UnboundJoin (join.asInstanceOf[Join[DomainA, DomainB, RangeA, RangeB]]) // TODO ugly typecast
}