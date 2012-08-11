package sae.syntax.sql

import impl.{Projection2, NoProjection, Projection}

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:53
 *
 */
object SELECT
{

    def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range): SELECT_CLAUSE[Domain, Range] =
        Projection (projection, distinct = false)

    def apply[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: (DomainA,DomainB) => Range): SELECT_CLAUSE_2[DomainA,DomainB, Range] =
        Projection2(projection, distinct = false)


    def apply(x: STAR): SELECT_CLAUSE_NO_PROJECTION = NoProjection (distinct = false)

    def DISTINCT[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range): SELECT_CLAUSE[Domain, Range] =
        Projection (projection, distinct = true)

    def DISTINCT(x: STAR): SELECT_CLAUSE_NO_PROJECTION = NoProjection (distinct = true)


}
