package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Date: 09.12.11
 * Time: 09:40
 *
 */
case class TypeQuery(name: String)
        extends UnissonQuery
{

    def isSyntacticEqual(query: UnissonQuery) = {
        query match {
            case TypeQuery(otherName) if (otherName == name) => true
            case _ => false
        }
    }

}