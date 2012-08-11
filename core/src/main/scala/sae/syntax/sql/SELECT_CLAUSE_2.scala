package sae.syntax.sql

import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:48
 *
 */
trait SELECT_CLAUSE_2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef]
{

    def FROM(relationA: LazyView[DomainA],relationB :LazyView[DomainB] ): FROM_CLAUSE_2[DomainA,DomainB,Range]

}