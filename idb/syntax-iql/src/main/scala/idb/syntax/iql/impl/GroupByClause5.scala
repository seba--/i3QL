package idb.syntax.iql.impl

import idb.syntax.iql._

import idb.syntax.iql.IR._

/**
 *
 * @author Mirko Köhler
 */
case class GroupByClause5[Select : Manifest,
	DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest, DomainE : Manifest,
	GroupDomainA : Manifest, GroupDomainB : Manifest, GroupDomainC : Manifest, GroupDomainD : Manifest, GroupDomainE : Manifest,
	GroupRange : Manifest, Range : Manifest
] (
	grouping : (Rep[GroupDomainA], Rep[GroupDomainB], Rep[GroupDomainC], Rep[GroupDomainD], Rep[GroupDomainE]) => Rep[GroupRange],
 	groupClause : CAN_GROUP_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, Range])
extends GROUP_BY_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupDomainE, GroupRange, Range] {

}

