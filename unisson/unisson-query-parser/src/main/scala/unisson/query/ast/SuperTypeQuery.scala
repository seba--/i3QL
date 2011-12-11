package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 10:12
 *
 */

case class SuperTypeQuery(subQuery: UnissonQuery)
        extends UnissonQuery
{

}