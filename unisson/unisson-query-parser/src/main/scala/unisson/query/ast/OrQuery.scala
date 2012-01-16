package unisson.query.ast

import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 17:41
 *
 */

case class OrQuery(left: UnissonQuery, right: UnissonQuery)
        extends UnissonQuery
{

    def isSyntacticEqual(query: UnissonQuery): Boolean = {
        query match {
            case other@OrQuery(_, _) => {
                var otherQueries = flattenOrQueries(other)
                var thisQueries = flattenOrQueries(this)
                if( thisQueries.size != otherQueries.size )
                {
                    return false
                }
                for (q <- thisQueries) {
                    var (matching, notmatching) = otherQueries.partition(_.isSyntacticEqual(q))
                    if( matching.isEmpty)
                    {
                        return false
                    }
                    otherQueries = matching.drop(1) ++ notmatching
                }
                true
            }
            case _ => false
        }
    }

    override def commonPrefixAndDifference(query: UnissonQuery) = {
        query match {
            case other@OrQuery(_, _) => {
                val otherQueries = flattenOrQueries(other)
                val thisQueries = flattenOrQueries(this)
                var commonPrefix: List[UnissonQuery] = Nil
                var rest: List[UnissonQuery] = Nil
                for (q <- otherQueries) {
                    if (thisQueries.find(_.isSyntacticEqual(q)).isDefined) {
                        commonPrefix ::= q
                    }
                    else
                    {
                        rest ::= q
                    }
                }
                (commonPrefix.reduce(OrQuery(_,_)), rest.reduce(OrQuery(_,_)))
            }
            case _ => (EmptyQuery(), query)
        }
    }

    private def flattenOrQueries(query: OrQuery): List[UnissonQuery] = {
        var result: List[UnissonQuery] = Nil
        if (query.left.isInstanceOf[OrQuery]) {
            result = result ::: flattenOrQueries(query.left.asInstanceOf[OrQuery])
        }
        else {
            result = result ::: List(query.left)
        }
        if (query.right.isInstanceOf[OrQuery]) {
            result = result ::: flattenOrQueries(query.right.asInstanceOf[OrQuery])
        }
        else {
            result = result ::: List(query.right)
        }
        result
    }

}