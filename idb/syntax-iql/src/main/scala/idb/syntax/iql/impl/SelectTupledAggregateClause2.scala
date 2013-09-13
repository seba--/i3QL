package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
case class SelectTupledAggregateClause2[Select : Manifest, DomainA : Manifest, DomainB : Manifest, RangeA : Manifest, RangeB : Manifest](
	columns : Rep[Select => RangeA],
	aggregation : AGGREGATE_FUNCTION_2[DomainA, DomainB, RangeB],
	asDistinct: Boolean = false
) extends SELECT_TUPLED_AGGREGATE_CLAUSE_2[Select, DomainA, DomainB, RangeA, RangeB] {

	def FROM (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]]
	) =	FromClause2 (relationA, relationB, this)



}
