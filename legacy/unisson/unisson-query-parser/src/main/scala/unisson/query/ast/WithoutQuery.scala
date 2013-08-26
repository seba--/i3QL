package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 17:41
 *
 */

case class WithoutQuery(left: UnissonQuery, right: UnissonQuery)
        extends UnissonQuery
{

    def isSyntacticEqual(query:UnissonQuery) : Boolean = {
        query match {
            case WithoutQuery(otherLeft, otherRight) if( left.isSyntacticEqual(otherLeft) && right.isSyntacticEqual(otherRight))=> true
            case _ => false
        }
    }
    
}