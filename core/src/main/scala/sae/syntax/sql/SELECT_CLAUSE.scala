package sae.syntax.sql

import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:48
 *
 */
trait SELECT_CLAUSE[Domain <: AnyRef, Range <: AnyRef]
{

    def FROM(relation : LazyView[Domain]) : FROM_CLAUSE[Domain, Range]

}