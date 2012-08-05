package sae.syntax

import sae.LazyView
import sae.collections.QueryResult

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:56
 *
 */
package object sql
{

    implicit def convertASTToQuery[Domain <: AnyRef](clause: SQL_END_CLAUSE[Domain]): LazyView[Domain] =
        clause.compile ()

    implicit def convertASTToQueryResult[Domain <: AnyRef](clause: SQL_END_CLAUSE[Domain]): QueryResult[Domain] =
        lazyViewToResult (clause.compile ())

    implicit def lazyViewToResult[V <: AnyRef](lazyView: LazyView[V]): QueryResult[V] = sae.collections.Conversions
        .lazyViewToResult (
        lazyView
    )

    implicit def functionTuples2[Domain, R1, R2](functionTuple: (Domain => R1, Domain => R2)): Domain => (R1, R2) =
        (x: Domain) => (functionTuple._1 (x), functionTuple._2 (x))

}