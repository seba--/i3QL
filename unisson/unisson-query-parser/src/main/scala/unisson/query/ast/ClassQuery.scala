package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 06.09.11 12:02
 *
 */

case class ClassQuery(subQuery: UnissonQuery)
        extends UnissonQuery
{

}