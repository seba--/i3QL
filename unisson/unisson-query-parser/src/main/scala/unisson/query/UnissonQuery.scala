package unisson.query

import ast.EmptyQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 10:12
 *
 */

trait UnissonQuery
{

    def isSyntacticEqual(query: UnissonQuery): Boolean

    /**
     * computes the longest common prefix of this query and the given query,
     * as well as
     * 1. the remainder of this query and
     * 2. the remainder of the other query and
     * 3. a concatenation function for compiled remainder queries
     */
    def commonPrefixAndDifference(query: UnissonQuery): (UnissonQuery,UnissonQuery) = {
        if (this.isSyntacticEqual(query)) {
            (this, EmptyQuery())
        }
        else {
            (EmptyQuery(), query)
        }
    }
}