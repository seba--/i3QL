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
case class FromClause3[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, Range : Manifest] (
    relationA : Rep[Query[DomainA]],
	relationB : Rep[Query[DomainB]],
	relationC : Rep[Query[DomainC]],
    selectClause : SELECT_CLAUSE[Select, Range]
)
    extends FROM_CLAUSE_3[Select, DomainA, DomainB, DomainC, Range]
    with CAN_GROUP_CLAUSE_3[Select, DomainA, DomainB, DomainC, Range]
{
	def WHERE (
		 predicate: (Rep[DomainA], Rep[DomainB], Rep[DomainC]) => Rep[Boolean]
	): WHERE_CLAUSE_3[Select, DomainA ,DomainB, DomainC, Range]
		with CAN_GROUP_CLAUSE_3[Select, DomainA, DomainB, DomainC, Range] =
		WhereClause3 (fun(predicate), this)

	def GROUP[GroupDomainA : Manifest, GroupDomainB : Manifest, GroupDomainC : Manifest, GroupRange : Manifest] (
		grouping: (Rep[GroupDomainA], Rep[GroupDomainB], Rep[GroupDomainC]) => Rep[GroupRange]
	): GROUP_BY_CLAUSE_3[Select, DomainA, DomainB, DomainC, GroupDomainA, GroupDomainB, GroupDomainC, GroupRange, Range] =
        GroupByClause3 (grouping, this)
}