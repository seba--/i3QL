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
case class Projection[SelectionDomain <: AnyRef, Range <: AnyRef](projection: SelectionDomain => Range, distinct: Boolean)
    extends SELECT_CLAUSE[SelectionDomain, Range]
{
    def FROM[Domain <: SelectionDomain](relation: LazyView[Domain]) = FromWithProjection (projection, relation, distinct)
}