package idb.syntax.iql.impl


import idb.syntax.iql.IR._
import idb.syntax.iql.{GROUP_BY_CLAUSE_1, CAN_GROUP_CLAUSE_1}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 08.08.13
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
case class GroupByClause1[Select : Manifest, Domain : Manifest, GroupDomain : Manifest, GroupRange : Manifest,  Range : Manifest] (
	grouping : Rep[GroupDomain => GroupRange],
 	groupClause : CAN_GROUP_CLAUSE_1[Select,Domain,Range])
extends GROUP_BY_CLAUSE_1[Select, Domain, GroupDomain, GroupRange, Range] {


}

