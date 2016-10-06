package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
case class SelectAggregateClause[Select : Manifest, Domain : Manifest, Range : Manifest](
	aggregation : AGGREGATE_FUNCTION[Domain, Range],
	asDistinct: Boolean = false
) extends SELECT_AGGREGATE_CLAUSE[Select, Domain, Range] {

	def FROM (
		relation : Rep[Query[Domain]]
	) =	FromClause1 (relation, this)

}
