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
case class FromClause2[Select : Manifest, DomainA: Manifest, DomainB : Manifest, Range: Manifest] (
    relationA : Rep[Query[DomainA]],
	relationB : Rep[Query[DomainB]],
    selectClause : SELECT_CLAUSE[Select, Range]
)
    extends FROM_CLAUSE_2[Select, DomainA, DomainB, Range]
    with CAN_GROUP_CLAUSE_2[Select, DomainA, DomainB, Range]
{
	def WHERE (
		 predicate: (Rep[DomainA], Rep[DomainB]) => Rep[Boolean]
	): WHERE_CLAUSE_2[Select, DomainA ,DomainB, Range]
		with CAN_GROUP_CLAUSE_2[Select, DomainA, DomainB, Range] =
		WhereClause2 (fun(predicate), this)

	def GROUP[GroupDomainA : Manifest, GroupDomainB : Manifest, GroupRange : Manifest] (
		grouping: (Rep[GroupDomainA], Rep[GroupDomainB]) => Rep[GroupRange]
	): GROUP_BY_CLAUSE_2[Select, DomainA, DomainB, GroupDomainA, GroupDomainB, GroupRange, Range] =
        GroupByClause2 (grouping, this)
}