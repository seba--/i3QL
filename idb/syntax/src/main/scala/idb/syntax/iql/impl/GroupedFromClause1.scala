package idb.syntax.iql.impl

import idb.syntax.iql.{CAN_GROUP_CLAUSE_1, GROUPED_FROM_CLAUSE_1}
import idb.syntax.iql.IR._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 08.08.13
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
/*
case class GroupedFromClause1[Group : Manifest, Domain : Manifest, Range : Manifest](
	relation : Rep[Query[Domain]],
	selectClause : SelectClause1[Group,Range]) extends GROUPED_FROM_CLAUSE_1[Group, Domain, Range] {

	def WHERE (predicate: Rep[Domain] => Rep[Boolean]) = WhereClause1[Group,Domain,Range](predicate,this)

}
*/