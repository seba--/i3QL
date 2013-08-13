package idb.syntax.iql.impl

import idb.syntax.iql._

import idb.syntax.iql.IR._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 08.08.13
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
case class GroupByClause2[Group : Manifest, DomainA : Manifest, DomainB : Manifest, Range : Manifest] (
	grouping : (Rep[(DomainA,DomainB)]) => Rep[Group],
 	groupClause : CAN_GROUP_CLAUSE_2[Group,DomainA, DomainB,Range])
extends GROUP_BY_CLAUSE_2[DomainA, DomainB, Range] {

}

