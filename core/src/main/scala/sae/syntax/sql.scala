package sae.syntax

import sae.LazyView
import sae.collections.QueryResult
import sql.impl.{JoinInfixOperator, InlineWhereClause}

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

    val NOT: NOT_KEYWORD = keywords.NOT_KEYWORD

    def NOT[Domain <: AnyRef](predicate: Domain => Boolean): Domain => Boolean = {
        x => !predicate (x)
    }

    implicit def compile[Domain <: AnyRef](clause: SQL_QUERY[Domain]): LazyView[Domain] =
        clause.compile ()

    implicit def lazyViewToResult[V <: AnyRef](lazyView: LazyView[V]): QueryResult[V] = sae.collections.Conversions
        .lazyViewToResult (
        lazyView
    )

    implicit def functionTuples2[Domain, R1, R2](functionTuple: (Domain => R1, Domain => R2)): Domain => (R1, R2) =
        (x: Domain) => (functionTuple._1 (x), functionTuple._2 (x))

    implicit def predicateToInlineWhereClause[Domain <: AnyRef](f: Domain => Boolean): INLINE_WHERE_CLAUSE[Domain] =
        InlineWhereClause (f)

    implicit def whereClaus2ToNextDomain[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](whereClause2 : WHERE_CLAUSE_2[DomainA, DomainB, Range]): WHERE_CLAUSE[DomainB, Range] =
        null

    // TODO reactor to ast
    implicit def inlineWhereClauseToPredicate[Domain <: AnyRef](clause: INLINE_WHERE_CLAUSE[Domain]): Domain => Boolean =
        clause.function

    implicit def functionToComparator[Domain, Range](f: Domain => Range): WHERE_FUNCTION_COMPARATOR[Domain, Range] = new WHERE_FUNCTION_COMPARATOR[Domain, Range] {
        def === (value: Range) = (x: Domain) => f (x) == value
    }

    implicit def functionToJoin[Domain <: AnyRef, Range <: AnyRef](left: Domain => Range): JOIN_INFIX_KEYWORD[Domain, Range] =
        JoinInfixOperator (left)

    implicit def joinToUnboundJoin[DomainA <: AnyRef, DomainB <: AnyRef, RangeA <: AnyRef, RangeB <: AnyRef](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, DomainB, RangeA, RangeB] =
        new JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, DomainB, RangeA, RangeB]
        {}
}