package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._


/**
 * @author Mirko Köhler
 */
case class SelectTupledAggregateClause5[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest, DomainE : Manifest, RangeA : Manifest, RangeB : Manifest](
	project : Rep[Select => RangeA],
	aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB, DomainC, DomainD, DomainE), RangeB],
	asDistinct: Boolean = false
) extends SELECT_TUPLED_AGGREGATE_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, RangeA, RangeB]
{

	def FROM (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]],
		relationD : Rep[Query[DomainD]],
		relationE : Rep[Query[DomainE]]
	) =
		aggregation match {
			case selfMaintained : AggregateFunctionSelfMaintained[(DomainA, DomainB, DomainC, DomainD, DomainE), RangeB] => {
				FromClause5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, (RangeA, RangeB)] (
					relationA,
					relationB,
					relationC,
					relationD,
					relationE,
					SelectAggregateClause (
						AggregateTupledFunctionSelfMaintained[Select, (DomainA, DomainB, DomainC, DomainD, DomainE), RangeA, RangeB, (RangeA, RangeB)] (
							selfMaintained.start,
							(x : Rep[((DomainA, DomainB, DomainC, DomainD, DomainE), RangeB)]) =>
								selfMaintained.added ((x._1, x._2)),
							(x : Rep[((DomainA, DomainB, DomainC, DomainD, DomainE), RangeB)]) =>
								selfMaintained.removed ((x._1, x._2)),
							(x : Rep[((DomainA, DomainB, DomainC, DomainD, DomainE), (DomainA, DomainB, DomainC, DomainD, DomainE), RangeB)]) =>
								selfMaintained.updated ((x._1, x._2, x._3)),
							project,
							fun ( (x : Rep[(RangeA, RangeB)]) => x )
						),
						asDistinct
					)
				)
			}

			case notSelfMaintained : AggregateFunctionNotSelfMaintained[(DomainA, DomainB, DomainC, DomainD, DomainE), RangeB] => {
				FromClause5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, (RangeA, RangeB)] (
					relationA,
					relationB,
					relationC,
					relationD,
					relationE,
					SelectAggregateClause (
						AggregateTupledFunctionNotSelfMaintained[Select, (DomainA, DomainB, DomainC, DomainD, DomainE), RangeA, RangeB, (RangeA, RangeB)] (
							notSelfMaintained.start,
							(x : Rep[((DomainA, DomainB, DomainC, DomainD, DomainE), RangeB, Seq[(DomainA, DomainB, DomainC, DomainD, DomainE)])]) =>
								notSelfMaintained.added ((x._1, x._2, x._3)),
							(x : Rep[((DomainA, DomainB, DomainC, DomainD, DomainE), RangeB, Seq[(DomainA, DomainB, DomainC, DomainD, DomainE)])]) =>
								notSelfMaintained.removed ((x._1, x._2, x._3)),
							(x : Rep[((DomainA, DomainB, DomainC, DomainD, DomainE), (DomainA, DomainB, DomainC, DomainD, DomainE), RangeB, Seq[(DomainA, DomainB, DomainC, DomainD, DomainE)])]) =>
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
