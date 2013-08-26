package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 10:10
 *
 */

case class PackageQuery(name: String)
        extends UnissonQuery
{

    def isSyntacticEqual(query: UnissonQuery) = {
        query match {
            case PackageQuery(otherName) if (otherName == name) => true
            case _ => false
        }
    }

}