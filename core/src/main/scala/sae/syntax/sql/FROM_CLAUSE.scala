package sae.syntax.sql

import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:51
 *
 */
trait FROM_CLAUSE[Domain <: AnyRef, Range <: AnyRef]
    extends SQL_END_CLAUSE[Range]
{

}