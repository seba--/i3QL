package idb.syntax.iql.impl

import idb.syntax.iql._

import idb.syntax.iql.IR._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 12.08.13
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
case class GroupedWhereClause2[Select : Manifest, DomainA : Manifest, DomainB : Manifest, Range : Manifest](
	predicate : (Rep[DomainA], Rep[DomainB]) => Rep[Boolean],
	fromClause : GROUPED_FROM_CLAUSE_2[Select, DomainA, DomainB, Range]
)
	extends CAN_GROUP_CLAUSE_2[Select, DomainA, DomainB, Range]
{

	def GROUP[GroupDomainA : Manifest, GroupDomainB : Manifest, GroupRange : Manifest] (
		grouping: (Rep[GroupDomainA], Rep[GroupDomainB]) => Rep[GroupRange]
	): GROUP_BY_CLAUSE_2[Select, DomainA, DomainB, GroupDomainA, GroupDomainB, GroupRange, Range] =
		GroupByClause2 (grouping, this)



}
