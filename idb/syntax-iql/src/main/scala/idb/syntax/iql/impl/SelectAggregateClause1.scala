package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
case class SelectAggregateClause1[Select : Manifest, Domain : Manifest, Range : Manifest](
	aggregation : AGGREGATE_FUNCTION_1[Domain, Range],
	asDistinct: Boolean = false
) extends SELECT_AGGREGATE_CLAUSE_1[Select, Domain, Range] {

	def FROM (
		relation : Rep[Query[Domain]]
	) =	FromClause1 (
			relation,
			SelectAggregateClause (
				aggregation,
			    asDistinct
			)
		)



}
