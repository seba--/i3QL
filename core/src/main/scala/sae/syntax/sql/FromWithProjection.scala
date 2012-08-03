package sae.syntax.sql

import sae.LazyView
import sae.operators.BagProjection

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
private[sql] case class FromWithProjection[Domain <: AnyRef, Range <: AnyRef](projection : Domain => Range, relation: LazyView[Domain])
        extends FROM_CLAUSE[Domain, Range]
{

    def compile() = new BagProjection[Domain, Range](projection, relation)
}