package sae.syntax.sql

import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:00
 *
 */
object NoProjection extends SELECT_CLAUSE_NO_PROJECTION
{
    def FROM[Domain <: AnyRef](relation: LazyView[Domain]) = FromNoProjection[Domain](relation)
}