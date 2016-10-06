package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._


/**
 * @author Mirko Köhler
 */
case class SelectTupledAggregateClause4[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest, RangeA : Manifest, RangeB : Manifest](
	project : Rep[Select => RangeA],
	aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB, DomainC, DomainD), RangeB],
	asDistinct: Boolean = false
) extends SELECT_TUPLED_AGGREGATE_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, RangeA, RangeB]
{

	def FROM (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]],
		relationD : Rep[Query[DomainD]]
	) =
		aggregation match {
			case selfMaintained : AggregateFunctionSelfMaintained[(DomainA, DomainB, DomainC, DomainD), RangeB] => {
				FromClause4[Select, DomainA, DomainB, DomainC, DomainD, (RangeA, RangeB)] (
					relationA,
					relationB,
					relationC,
					relationD,
					SelectAggregateClause (
						AggregateTupledFunctionSelfMaintained[Select, (DomainA, DomainB, DomainC, DomainD), RangeA, RangeB, (RangeA, RangeB)] (
							selfMaintained.start,
							(x : Rep[((DomainA, DomainB, DomainC, DomainD), RangeB)]) =>
								selfMaintained.added ((x._1, x._2)),
							(x : Rep[((DomainA, DomainB, DomainC, DomainD), RangeB)]) =>
								selfMaintained.removed ((x._1, x._2)),
							(x : Rep[((DomainA, DomainB, DomainC, DomainD), (DomainA, DomainB, DomainC, DomainD), RangeB)]) =>
								selfMaintained.updated ((x._1, x._2, x._3)),
							project,
							fun ( (x : Rep[(RangeA, RangeB)]) => x )
						),
						asDistinct
					)
				)
			}

			case notSelfMaintained : AggregateFunctionNotSelfMaintained[(DomainA, DomainB, DomainC, DomainD), RangeB] => {
				FromClause4[Select, DomainA, DomainB, DomainC, DomainD, (RangeA, RangeB)] (
					relationA,
					relationB,
					relationC,
					relationD,
					SelectAggregateClause (
						AggregateTupledFunctionNotSelfMaintained[Select, (DomainA, DomainB, DomainC, DomainD), RangeA, RangeB, (RangeA, RangeB)] (
							notSelfMaintained.start,
							(x : Rep[((DomainA, DomainB, DomainC, DomainD), RangeB, Seq[(DomainA, DomainB, DomainC, DomainD)])]) =>
								notSelfMaintained.added ((x._1, x._2, x._3)),
							(x : Rep[((DomainA, DomainB, DomainC, DomainD), RangeB, Seq[(DomainA, DomainB, DomainC, DomainD)])]) =>
								notSelfMaintained.removed ((x._1, x._2, x._3)),
							(x : Rep[((DomainA, DomainB, DomainC, DomainD), (DomainA, DomainB, DomainC, DomainD), RangeB, Seq[(DomainA, DomainB, DomainC, DomainD)])]) =>
								notSelfMaintained.updated ((x._1, x._2, x._3, x._4)),
							project,
							fun ( (x : Rep[(RangeA, RangeB)]) => x )
						),
						asDistinct
					)
				)
			}
		}


}
