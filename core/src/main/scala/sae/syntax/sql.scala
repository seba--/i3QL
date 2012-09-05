package sae.syntax

import sae.LazyView
import sae.collections.QueryResult
import sql.ast.Filter
import sql.impl.{WhereClause2Expression, WhereClauseComparator, WhereClause1Expression}

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
        null


    implicit def functionToComparator[Domain <: AnyRef, Range](left: Domain => Range): WHERE_CLAUSE_COMPARATOR[Domain, Range] =
        WhereClauseComparator (left)


    implicit def functionTuples2[Domain, R1, R2](functionTuple: (Domain => R1, Domain => R2)): Domain => (R1, R2) =
        (x: Domain) => (functionTuple._1 (x), functionTuple._2 (x))

    implicit def predicateToInlineWhereClause[Domain <: AnyRef](f: Domain => Boolean): WHERE_CLAUSE_EXPRESSION_START[Domain] =
        WhereClause1Expression (Seq (Filter (f)))

    implicit def joinToInlineWhereClause[DomainA <: AnyRef, DomainB <: AnyRef, RangeA, RangeB](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_EXPRESSION_START_2[DomainA, DomainB] =
        WhereClause2Expression (Seq (join))


    /*
    implicit def functionToJoin[Domain <: AnyRef, Range](left: Domain => Range): JOIN_INFIX_KEYWORD[Domain, Range] =
        JoinInfixOperator (left)
    */

    implicit def joinToUnboundJoin[DomainA <: AnyRef, DomainB <: AnyRef, RangeA <: AnyRef, RangeB <: AnyRef](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, DomainB, RangeA, RangeB] =
        new JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, DomainB, RangeA, RangeB]
        {}
}