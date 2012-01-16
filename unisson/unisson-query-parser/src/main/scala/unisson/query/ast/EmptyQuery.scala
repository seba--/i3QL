package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 02.09.11 10:43
 *
 */

case class EmptyQuery()
        extends UnissonQuery
{

    def isSyntacticEqual(query: UnissonQuery) = query.isInstanceOf[EmptyQuery]

}