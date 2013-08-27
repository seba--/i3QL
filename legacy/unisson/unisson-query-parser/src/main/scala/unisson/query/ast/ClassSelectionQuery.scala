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

    def isSyntacticEqual(query: UnissonQuery) = {
        query match {
            case ClassSelectionQuery(otherPackageName, otherName) if (otherPackageName == packageName && otherName == name) => true
            case _ => false
        }
    }

}