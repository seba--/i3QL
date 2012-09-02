package sae.syntax.sql.ast

import sae.LazyView


/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromClause2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](selectClause: SelectClause2[_ >: DomainA, _ >: DomainB, Range],
                                                                              relationA: LazyView[DomainA],
                                                                              relationB: LazyView[DomainB])
{

}