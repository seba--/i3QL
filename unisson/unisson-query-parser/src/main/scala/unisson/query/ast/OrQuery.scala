package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 17:41
 *
 */

case class OrQuery(left: UnissonQuery, right: UnissonQuery)
        extends UnissonQuery
{

}