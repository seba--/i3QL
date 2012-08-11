package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql.{DISTINCT_NO_PROJECTION, STAR, DISTINCT_PROJECTION, STARTING_FROM_CLAUSE}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 15:35
 */

case class FromStarting[Domain <: AnyRef](relation: LazyView[Domain])
    extends STARTING_FROM_CLAUSE[Domain]
{
    def SELECT[Range <: AnyRef](projection: (Domain) => Range) = FromWithProjection (projection, relation, distinct = false)

    def SELECT(x: STAR) = FromNoProjection (relation, distinct = false)

    def SELECT[Range <: AnyRef](distinct: DISTINCT_PROJECTION[Domain, Range]) = FromWithProjection (distinct.function, relation, distinct = true)

    def SELECT(distinct: DISTINCT_NO_PROJECTION.type) = FromNoProjection (relation, distinct = true)
}