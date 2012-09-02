package sae.syntax.sql.ast

import sae.LazyView


/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromClause1[Domain <: AnyRef, Range <: AnyRef](selectClause: SelectClause1[_ >: Domain, Range], relation: LazyView[Domain])
{

}