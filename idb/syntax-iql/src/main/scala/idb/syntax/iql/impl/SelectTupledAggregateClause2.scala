package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._


/**
 * @author Mirko Köhler
 */
case class SelectTupledAggregateClause2[Select : Manifest, DomainA : Manifest, DomainB : Manifest, RangeA : Manifest, RangeB : Manifest](
	project : Rep[Select => RangeA],
	aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB), RangeB],
	asDistinct: Boolean = false
) extends SELECT_TUPLED_AGGREGATE_CLAUSE_2[Select, DomainA, DomainB, RangeA, RangeB]
{

	def FROM (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]]
	) =
		aggregation match {
			case selfMaintained : AggregateFunctionSelfMaintained[(DomainA, DomainB), RangeB] => {
				FromClause2[Select, DomainA, DomainB, (RangeA, RangeB)] (
					relationA,
					relationB,
					SelectAggregateClause (
						AggregateTupledFunctionSelfMaintained[Select, (DomainA, DomainB), (RangeA, RangeB)] (
							selfMaintained.start,
							(x : Rep[((DomainA, DomainB), Any)]) => selfMaintained.added ((x._1, x._2.asInstanceOf[Rep[RangeB]])),
							(x : Rep[((DomainA, DomainB), Any)]) => selfMaintained.removed ((x._1, x._2.asInstanceOf[Rep[RangeB]])),
							(x : Rep[((DomainA, DomainB), (DomainA, DomainB), Any)]) => selfMaintained.updated ((x._1, x._2, x._3.asInstanceOf[Rep[RangeB]])),
							project
						)
					)
				)
			}

			case notSelfMaintained : AggregateFunctionNotSelfMaintained[(DomainA, DomainB), RangeB] => {
				FromClause2[Select, DomainA, DomainB, (RangeA, RangeB)] (
					relationA,
					relationB,
					SelectAggregateClause (
						AggregateTupledFunctionNotSelfMaintained[Select, (DomainA, DomainB), (RangeA, RangeB)] (
							notSelfMaintained.start,
							(x : Rep[((DomainA, DomainB), Any, Iterable[(DomainA, DomainB)])]) => notSelfMaintained.added ((x._1, x._2.asInstanceOf[Rep[RangeB]], x._3)),
							(x : Rep[((DomainA, DomainB), Any, Iterable[(DomainA, DomainB)])]) => notSelfMaintained.removed ((x._1, x._2.asInstanceOf[Rep[RangeB]], x._3)),
							(x : Rep[((DomainA, DomainB), (DomainA, DomainB), Any, Iterable[(DomainA, DomainB)])]) => notSelfMaintained.updated ((x._1, x._2, x._3.asInstanceOf[Rep[RangeB]], x._4)),
							project
						)
					)
				)
			}
		}


}
