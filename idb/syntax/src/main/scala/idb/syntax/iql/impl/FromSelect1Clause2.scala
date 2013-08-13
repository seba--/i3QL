package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 * The syntax representation of a from clause for one relation.
 *
 */
case class FromSelect1Clause2[Select : Manifest, DomainA: Manifest, DomainB : Manifest, Range: Manifest] (
    relationA : Rep[Query[DomainA]],
	relationB : Rep[Query[DomainB]],
    selectClause: SelectClause1[Select, Range]
)
    extends CAN_GROUP_CLAUSE_2[Select, DomainA, DomainB, Range]
	with GROUPED_FROM_CLAUSE_2[Select, DomainA, DomainB, Range]
{
	def WHERE (
		 predicate: (Rep[DomainA], Rep[DomainB]) => Rep[Boolean]
	): CAN_GROUP_CLAUSE_2[Select, DomainA, DomainB, Range] =
		GroupedWhereClause2(predicate, this)

    def GROUP (
        grouping: (Rep[(DomainA,DomainB)]) => Rep[Select]
    ): GROUP_BY_CLAUSE_2[DomainA, DomainB, Range] =
        GroupByClause2(grouping, this)
}