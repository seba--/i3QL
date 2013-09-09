package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 *@author Mirko Köhler
 */
case class SelectAggregateClause[Select : Manifest, AggregateDomain : Manifest, RangeA : Manifest, RangeB : Manifest](
	projection : Rep[Select => RangeA],
	aggregation : AGGREGATE_FUNCTION[AggregateDomain, RangeB]
) extends SELECT_CLAUSE[Select,(RangeA, RangeB)] {

	def FROM[Domain <: AggregateDomain : Manifest] (
		relation : Rep[Query[Domain]]
	) =	FromClause1 (relation, this)

	def FROM[DomainA : Manifest, DomainB : Manifest] (
		relationA: Rep[Query[DomainA]],
		relationB: Rep[Query[DomainB]]
	) = FromClause2 (relationA, relationB, this)

	def FROM[DomainA : Manifest, DomainB : Manifest, DomainC : Manifest] (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]]
	) = FromClause3 (relationA, relationB, relationC, this)

	def FROM[DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest] (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]],
		relationD : Rep[Query[DomainD]]
	) = FromClause4 (relationA, relationB, relationC, relationD, this)

	def FROM[DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest, DomainE : Manifest] (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]],
		relationD : Rep[Query[DomainD]],
		relationE : Rep[Query[DomainE]]
	) = FromClause5 (relationA, relationB, relationC, relationD, relationE, this)



}
