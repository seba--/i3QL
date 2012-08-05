package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql.SELECT_CLAUSE

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:55
 *
 */
case class Projection[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range, distinct: Boolean = false)
    extends SELECT_CLAUSE[Domain, Range]
{
    def FROM(relation: LazyView[Domain]) = FromWithProjection (projection, relation, distinct)
}