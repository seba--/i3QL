package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 10:11
 *
 */
case class ClassSelectionQuery(packageName: String, name: String)
        extends UnissonQuery
{

}