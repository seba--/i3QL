package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
case class SelectTupledAggregateClause1[Select : Manifest, Domain : Manifest, RangeA : Manifest, RangeB : Manifest](
	columns : Rep[Select => RangeA],
	aggregation : AGGREGATE_FUNCTION_1[Domain, RangeB],
	asDistinct: Boolean = false
) extends SELECT_TUPLED_AGGREGATE_CLAUSE_1[Select, Domain, RangeA, RangeB] {

	def FROM (
		relation : Rep[Query[Domain]]
	) =	FromClause1 (relation, this)



}
