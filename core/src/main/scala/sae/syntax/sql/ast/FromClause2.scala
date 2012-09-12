package sae.syntax.sql.ast

import sae.LazyView


/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromClause2[FromDomainA <: AnyRef, FromDomainB <: AnyRef](relationA: LazyView[FromDomainA],
                                                                     relationB: LazyView[FromDomainB])
    extends FromClause
{

    type DomainA = FromDomainA

    type DomainB = FromDomainB

}