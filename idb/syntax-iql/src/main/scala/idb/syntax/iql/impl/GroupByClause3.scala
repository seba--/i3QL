package idb.syntax.iql.impl

import idb.syntax.iql._

import idb.syntax.iql.IR._

/**
 *
 * @author Mirko Köhler
 */
case class GroupByClause3[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, GroupDomainA : Manifest, GroupDomainB : Manifest, GroupDomainC : Manifest, GroupRange : Manifest, Range : Manifest] (
	grouping : (Rep[GroupDomainA], Rep[GroupDomainB], Rep[GroupDomainC]) => Rep[GroupRange],
 	groupClause : CAN_GROUP_CLAUSE_3[Select, DomainA, DomainB, DomainC, Range])
extends GROUP_BY_CLAUSE_3[Select, DomainA, DomainB, DomainC, GroupDomainA, GroupDomainB, GroupDomainC, GroupRange, Range] {

}

