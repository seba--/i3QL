package idb.syntax.iql.impl

import idb.Relation
import idb.syntax.iql.{WHERE_CLAUSE_1, FROM_CLAUSE_1}

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 * The syntax representation of a from clause for one relation.
 *
 */
case class FromClause1[Domain, Range] (relation: Relation[Domain],
                                       selectClause: SelectClause1[Domain, Range]
                                      )
    extends FROM_CLAUSE_1[Domain, Range]
{
    def WHERE (predicate: (Domain) => Boolean): WHERE_CLAUSE_1[Domain, Range] =
        throw new UnsupportedOperationException()
}