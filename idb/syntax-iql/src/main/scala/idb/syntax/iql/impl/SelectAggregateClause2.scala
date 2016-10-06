package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
case class SelectAggregateClause2[Select : Manifest, DomainA : Manifest, DomainB : Manifest, Range : Manifest](
	aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB), Range],
	asDistinct: Boolean = false
) extends SELECT_AGGREGATE_CLAUSE_2[Select, DomainA, DomainB, Range] {

	def FROM (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]]
	) =	FromClause2 (
			relationA,
			relationB,
			SelectAggregateClause (
				aggregation,
			    asDistinct
			)
		)



}
