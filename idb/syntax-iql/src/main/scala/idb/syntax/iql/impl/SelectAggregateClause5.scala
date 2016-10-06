package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
case class SelectAggregateClause5[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest, DomainE : Manifest, Range : Manifest](
	aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB, DomainC, DomainD, DomainE), Range],
	asDistinct: Boolean = false
) extends SELECT_AGGREGATE_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, Range] {

	def FROM (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]],
		relationD : Rep[Query[DomainD]],
		relationE : Rep[Query[DomainE]]
	) =	FromClause5 (
			relationA,
			relationB,
			relationC,
			relationD,
		    relationE,
			SelectAggregateClause (
				aggregation,
			    asDistinct
			)
		)



}
