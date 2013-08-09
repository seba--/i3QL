package idb.syntax.iql.impl

import idb.syntax.iql.{CAN_GROUP_CLAUSE_1, GROUP_BY_CLAUSE_1}

import idb.syntax.iql.IR._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 08.08.13
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
case class GroupByFromClause1[Group : Manifest, Domain : Manifest, Range : Manifest] (
	grouping : (Rep[Domain]) => Rep[Group],
 	fromClause : FromClause1[Group,Domain,Range])
extends GROUP_BY_CLAUSE_1[Domain, Range] {

}

