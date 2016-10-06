package idb.syntax.iql.impl


import idb.syntax.iql.IR._
import idb.syntax.iql.{GROUP_BY_CLAUSE_1, CAN_GROUP_CLAUSE_1}

/**
 * @author Mirko Köhler
 */
case class GroupByClause1[Select : Manifest, Domain : Manifest, GroupDomain : Manifest, GroupRange : Manifest,  Range : Manifest] (
	grouping : Rep[GroupDomain => GroupRange],
 	groupClause : CAN_GROUP_CLAUSE_1[Select,Domain,Range])
extends GROUP_BY_CLAUSE_1[Select, Domain, GroupDomain, GroupRange, Range] {


}

