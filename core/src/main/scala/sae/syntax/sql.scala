package sae.syntax

import sae.LazyView
import sae.collections.QueryResult
import sql.impl.InlineWhereClause

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

    val EXISTS : EXISTS_KEYWORD = keywords.EXISTS_KEYWORD

    val NOT : NOT_KEYWORD = keywords.NOT_KEYWORD

    def NOT[Domain <: AnyRef](predicate: Domain => Boolean): Domain => Boolean = {x => !predicate (x)}

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

    implicit def inlineWhereClauseToPredicate[Domain <: AnyRef](clause: INLINE_WHERE_CLAUSE[Domain]): Domain => Boolean =
        clause.function

    implicit def functionToComparator[Domain, Range](f: Domain => Range): WHERE_FUNCTION_COMPARATOR[Domain, Range] = new WHERE_FUNCTION_COMPARATOR[Domain, Range] {
        def === (value: Range) = (x: Domain) => f (x) == value
    }

    implicit def functionToJoin[Domain, Range](f: Domain => Range): WHERE_FUNCTION_JOIN[Domain, Range] = null
}