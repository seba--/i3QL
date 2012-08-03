package sae.syntax.sql

import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
private[sql] case class FromNoProjection[Domain <: AnyRef](relation: LazyView[Domain])
        extends FROM_CLAUSE[Domain, Domain] with STARTING_FROM_CLAUSE[Domain]
{

    def compile() = relation

    def SELECT[Range <: AnyRef](projection: (Domain) => Range) = FromWithProjection(projection, relation)

    def SELECT(x: STAR) = this
}