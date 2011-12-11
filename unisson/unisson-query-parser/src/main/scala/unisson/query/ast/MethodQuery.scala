package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Date: 09.12.11
 * Time: 09:41
 *
 */
case class MethodQuery(classQuery: UnissonQuery,
                       name: String,
                       returnType: UnissonQuery,
                       parameters: UnissonQuery*)
        extends UnissonQuery
{

}