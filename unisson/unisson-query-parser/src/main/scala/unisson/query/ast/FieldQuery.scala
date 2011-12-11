package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Date: 09.12.11
 * Time: 09:42
 *
 */
case class FieldQuery(classQuery: UnissonQuery,
                      name: String,
                      fieldType: UnissonQuery)
        extends UnissonQuery
{

}