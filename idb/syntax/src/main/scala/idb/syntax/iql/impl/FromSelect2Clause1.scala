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
case class FromSelect2Clause1[SelectA : Manifest, SelectB : Manifest, Domain : Manifest, Range: Manifest] (
	relation : Rep[Query[Domain]],
    selectClause: SelectClause2[SelectA, SelectB, Range]
)
    extends CAN_GROUP_CLAUSE_1[(SelectA, SelectB), Domain, Range]
	with GROUPED_FROM_CLAUSE_1[(SelectA, SelectB), Domain, Range]
{
	def WHERE (
		 predicate: Rep[Domain] => Rep[Boolean]
	): CAN_GROUP_CLAUSE_1[(SelectA, SelectB), Domain, Range] =
		GroupedWhereClause1(predicate, this)

    def GROUP (
        grouping: Rep[Domain] => Rep[(SelectA, SelectB)]
    ): GROUP_BY_CLAUSE_1[Domain, Range] =
        GroupByClause1(grouping, this)
}