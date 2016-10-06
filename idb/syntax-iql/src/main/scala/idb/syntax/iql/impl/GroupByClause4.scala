package idb.syntax.iql.impl

import idb.syntax.iql._

import idb.syntax.iql.IR._

/**
 *
 * @author Mirko Köhler
 */
case class GroupByClause4[Select : Manifest,
	DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest,
	GroupDomainA : Manifest, GroupDomainB : Manifest, GroupDomainC : Manifest, GroupDomainD : Manifest,
	GroupRange : Manifest, Range : Manifest
] (
	grouping : (Rep[GroupDomainA], Rep[GroupDomainB], Rep[GroupDomainC], Rep[GroupDomainD]) => Rep[GroupRange],
 	groupClause : CAN_GROUP_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, Range])
extends GROUP_BY_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupRange, Range] {

}

