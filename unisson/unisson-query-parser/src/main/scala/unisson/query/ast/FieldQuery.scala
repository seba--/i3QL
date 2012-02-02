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


    def isSyntacticEqual(query: UnissonQuery) = {
        query match {
            case FieldQuery(otherClassQuery, otherName, otherFieldType) if
            (classQuery.isSyntacticEqual(otherClassQuery) && otherName == name && fieldType.isSyntacticEqual(otherFieldType)) => true
            case _ => false
        }
    }

}