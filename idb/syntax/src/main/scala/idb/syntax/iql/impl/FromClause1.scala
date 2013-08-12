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
case class FromClause1[Select: Manifest, Domain: Manifest, Range: Manifest] (
    relation: Rep[Query[Domain]],
    selectClause: SelectClause1[Select, Range]
)
    extends FROM_CLAUSE_1[Select, Domain, Range]
    with CAN_GROUP_CLAUSE_1[Select, Domain, Range]
	with GROUPED_FROM_CLAUSE_1[Select, Domain, Range]
{
    def WHERE (
        predicate: Rep[Domain] => Rep[Boolean]
    ): WHERE_CLAUSE_1[Select, Domain, Range] =
        WhereClause1 (predicate, this)

    def GROUP (
        grouping: (Rep[Domain]) => Rep[Select]
    ): GROUP_BY_CLAUSE_1[Domain, Range] =
        GroupByClause1(grouping, this)

}