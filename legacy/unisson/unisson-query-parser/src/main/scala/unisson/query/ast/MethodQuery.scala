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

    def isSyntacticEqual(query: UnissonQuery): Boolean = {
        query match {
            case MethodQuery(otherClassQuery, otherName, otherReturnType, otherParameters@_*) if
            (classQuery.isSyntacticEqual(otherClassQuery) &&
                    otherName == name &&
                    returnType.isSyntacticEqual(otherReturnType) &&
                    parameters.length == otherParameters.length) => {
                for (i <- 0 to (parameters.length - 1)) {
                    if (!parameters(i).isSyntacticEqual(otherParameters(i))) {
                        return false
                    }
                }
                true
            }
            case _ => false
        }
    }

}