package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
case class SelectAggregateClause3[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, Range : Manifest](
	aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB, DomainC), Range],
	asDistinct: Boolean = false
) extends SELECT_AGGREGATE_CLAUSE_3[Select, DomainA, DomainB, DomainC, Range] {

	def FROM (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]]
	) =	FromClause3 (
			relationA,
			relationB,
			relationC,
			SelectAggregateClause (
				aggregation,
			    asDistinct
			)
		)



}
