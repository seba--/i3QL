package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 *
 * The syntax representation of a from clause for one relation.
 *
 * @author Ralf Mitschke, Mirko Köhler
 *
 */
case class FromClause5[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest,
	DomainD : Manifest, DomainE : Manifest, Range : Manifest]
(
    relationA : Rep[Query[DomainA]],
	relationB : Rep[Query[DomainB]],
	relationC : Rep[Query[DomainC]],
	relationD : Rep[Query[DomainD]],
	relationE : Rep[Query[DomainE]],
    selectClause : SELECT_CLAUSE[Select, Range]
)
    extends FROM_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, Range]
    with CAN_GROUP_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, Range]
{
	def WHERE (
		 predicate: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD], Rep[DomainE]) => Rep[Boolean]
	): WHERE_CLAUSE_5[Select, DomainA ,DomainB, DomainC, DomainD, DomainE, Range]
		with CAN_GROUP_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, Range] =
		WhereClause5 (fun(predicate), this)

	def GROUP[GroupDomainA : Manifest, GroupDomainB : Manifest, GroupDomainC : Manifest, GroupDomainD : Manifest, GroupDomainE : Manifest, GroupRange : Manifest] (
		grouping: (Rep[GroupDomainA], Rep[GroupDomainB], Rep[GroupDomainC], Rep[GroupDomainD], Rep[GroupDomainE]) => Rep[GroupRange]
	): GROUP_BY_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupDomainE, GroupRange, Range] =
        GroupByClause5 (grouping, this)
}