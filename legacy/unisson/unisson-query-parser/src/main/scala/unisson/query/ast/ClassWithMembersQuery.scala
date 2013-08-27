package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 10:11
 *
 */

case class ClassWithMembersQuery(classQuery: UnissonQuery)
        extends UnissonQuery
{

    def isSyntacticEqual(query: UnissonQuery) = {
        query match {
            case ClassWithMembersQuery(otherSubQuery) if classQuery.isSyntacticEqual(otherSubQuery) => true
            case _ => false
        }
    }
}