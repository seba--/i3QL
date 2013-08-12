package idb.syntax.iql.impl

import idb.syntax.iql._

import idb.syntax.iql.IR._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 12.08.13
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
case class GroupedWhereClause1[Group : Manifest, Domain : Manifest, Range : Manifest](
	predicate : Rep[Domain] => Rep[Boolean],
	fromClause : GROUPED_FROM_CLAUSE_1[Group, Domain, Range]
)
	extends CAN_GROUP_CLAUSE_1[Group, Domain, Range]
{

	def GROUP(
		grouping: Rep[Domain] => Rep[Group]
	): GROUP_BY_CLAUSE_1[Domain, Range] =
		GroupByClause1 (grouping, this)



}
