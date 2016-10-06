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
case class FromClause4[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest, Range : Manifest] (
    relationA : Rep[Query[DomainA]],
	relationB : Rep[Query[DomainB]],
	relationC : Rep[Query[DomainC]],
	relationD : Rep[Query[DomainD]],
    selectClause : SELECT_CLAUSE[Select, Range]
)
    extends FROM_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, Range]
    with CAN_GROUP_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, Range]
{
	def WHERE (
		 predicate: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD]) => Rep[Boolean]
	): WHERE_CLAUSE_4[Select, DomainA ,DomainB, DomainC, DomainD, Range]
		with CAN_GROUP_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, Range] =
		WhereClause4 (fun(predicate), this)

	def GROUP[GroupDomainA : Manifest, GroupDomainB : Manifest, GroupDomainC : Manifest, GroupDomainD : Manifest, GroupRange : Manifest] (
		grouping: (Rep[GroupDomainA], Rep[GroupDomainB], Rep[GroupDomainC], Rep[GroupDomainD]) => Rep[GroupRange]
	): GROUP_BY_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupRange, Range] =
        GroupByClause4 (grouping, this)
}