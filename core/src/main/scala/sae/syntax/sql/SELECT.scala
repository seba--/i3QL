package sae.syntax.sql

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:53
 *
 */
object SELECT
{

    def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range) : SELECT_CLAUSE[Domain, Range] =
        Projection(projection)

    def apply(x:STAR) : SELECT_CLAUSE_NO_PROJECTION = NoProjection


}
