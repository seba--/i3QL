package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
case class SelectAggregateClause4[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest, Range : Manifest](
	aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB, DomainC, DomainD), Range],
	asDistinct: Boolean = false
) extends SELECT_AGGREGATE_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, Range] {

	def FROM (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]],
		relationD : Rep[Query[DomainD]]
	) =	FromClause4 (
			relationA,
			relationB,
			relationC,
			relationD,
			SelectAggregateClause (
				aggregation,
			    asDistinct
			)
		)



}
