package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 * The syntax representation of a from clause for one relation.
 *
 */
case class FromClause1[Domain: Manifest, Range: Manifest] (
    relation: Rep[Query[Domain]],
    selectClause: SelectClause1[Domain, Range]
)
    extends FROM_CLAUSE_1[Domain, Range]
    with CAN_GROUP_CLAUSE_1[Domain, Domain, Range]
{
    def WHERE (predicate: Rep[Domain] => Rep[Boolean]): WHERE_CLAUSE_1[Domain, Range] =
        WhereClause1 (predicate, this)

    def GROUP (
        grouping: (IR.Rep[Domain]) => IR.Rep[Domain]
    ): GROUP_BY_CLAUSE_1[Domain, Range] =
        throw new UnsupportedOperationException

}