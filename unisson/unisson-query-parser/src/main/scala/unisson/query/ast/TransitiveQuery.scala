package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 10:13
 *
 */

case class TransitiveQuery(subQuery: UnissonQuery)
        extends UnissonQuery
{

}