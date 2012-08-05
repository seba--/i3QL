package sae.syntax.sql

import impl.FromStarting
import sae.LazyView

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 21:15
 *
 */
object FROM
{

    def apply[Domain <: AnyRef](relation: LazyView[Domain]): STARTING_FROM_CLAUSE[Domain] =
        FromStarting (relation)

}