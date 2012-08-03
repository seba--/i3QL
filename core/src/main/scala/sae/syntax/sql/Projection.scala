package sae.syntax.sql

import sae.LazyView
import sae.operators.BagProjection

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:55
 *
 */
private[sql] case class Projection[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range) extends SELECT_CLAUSE[Domain, Range]
{
    def FROM(relation: LazyView[Domain]) = FromWithProjection(projection, relation)
}