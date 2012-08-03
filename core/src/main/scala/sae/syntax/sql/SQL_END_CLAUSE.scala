package sae.syntax.sql

import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:57
 *
 */
trait SQL_END_CLAUSE[Domain <: AnyRef]
{

    def compile() : LazyView[Domain]

}