package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._


/**
 * @author Mirko Köhler
 */
case class SelectTupledAggregateClause3[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, RangeA : Manifest, RangeB : Manifest](
	project : Rep[Select => RangeA],
	aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB, DomainC), RangeB],
	asDistinct: Boolean = false
) extends SELECT_TUPLED_AGGREGATE_CLAUSE_3[Select, DomainA, DomainB, DomainC, RangeA, RangeB]
{

	def FROM (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]]
	) =
		aggregation match {
			case selfMaintained : AggregateFunctionSelfMaintained[(DomainA, DomainB, DomainC), RangeB] => {
				FromClause3[Select, DomainA, DomainB, DomainC, (RangeA, RangeB)] (
					relationA,
					relationB,
					relationC,
					SelectAggregateClause (
						AggregateTupledFunctionSelfMaintained[Select, (DomainA, DomainB, DomainC), (RangeA, RangeB)] (
							selfMaintained.start,
							(x : Rep[((DomainA, DomainB, DomainC), Any)]) => selfMaintained.added ((x._1, x._2.asInstanceOf[Rep[RangeB]])),
							(x : Rep[((DomainA, DomainB, DomainC), Any)]) => selfMaintained.removed ((x._1, x._2.asInstanceOf[Rep[RangeB]])),
							(x : Rep[((DomainA, DomainB, DomainC), (DomainA, DomainB, DomainC), Any)]) => selfMaintained.updated ((x._1, x._2, x._3.asInstanceOf[Rep[RangeB]])),
							project
						)
					)
				)
			}

			case notSelfMaintained : AggregateFunctionNotSelfMaintained[(DomainA, DomainB, DomainC), RangeB] => {
				FromClause3[Select, DomainA, DomainB, DomainC, (RangeA, RangeB)] (
					relationA,
					relationB,
					relationC,
					SelectAggregateClause (
						AggregateTupledFunctionNotSelfMaintained[Select, (DomainA, DomainB, DomainC), (RangeA, RangeB)] (
							notSelfMaintained.start,
							(x : Rep[((DomainA, DomainB, DomainC), Any, Iterable[(DomainA, DomainB, DomainC)])]) => notSelfMaintained.added ((x._1, x._2.asInstanceOf[Rep[RangeB]], x._3)),
							(x : Rep[((DomainA, DomainB, DomainC), Any, Iterable[(DomainA, DomainB, DomainC)])]) => notSelfMaintained.removed ((x._1, x._2.asInstanceOf[Rep[RangeB]], x._3)),
							(x : Rep[((DomainA, DomainB, DomainC), (DomainA, DomainB, DomainC), Any, Iterable[(DomainA, DomainB, DomainC)])]) => notSelfMaintained.updated ((x._1, x._2, x._3.asInstanceOf[Rep[RangeB]], x._4)),
							project
						)
					)
				)
			}
		}


}
