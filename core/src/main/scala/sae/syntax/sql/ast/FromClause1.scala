package sae.syntax.sql.ast

import sae.LazyView


/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromClause1[FromDomain <: AnyRef](relation: LazyView[FromDomain])
    extends FromClause
{
    type Domain = FromDomain
}