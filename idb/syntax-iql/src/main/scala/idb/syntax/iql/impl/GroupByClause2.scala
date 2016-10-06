package idb.syntax.iql.impl

import idb.syntax.iql._

import idb.syntax.iql.IR._

/**
 *
 * @author Mirko Köhler
 */
case class GroupByClause2[Select : Manifest, DomainA : Manifest, DomainB : Manifest, GroupDomainA : Manifest, GroupDomainB : Manifest, GroupRange : Manifest, Range : Manifest] (
	grouping : Rep[((GroupDomainA, GroupDomainB)) => GroupRange],
 	groupClause : CAN_GROUP_CLAUSE_2[Select,DomainA, DomainB,Range])
extends GROUP_BY_CLAUSE_2[Select, DomainA, DomainB, GroupDomainA, GroupDomainB, GroupRange, Range] {

}

