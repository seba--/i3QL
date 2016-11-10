package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._


/**
 * @author Mirko Köhler
 */
case class SelectTupledAggregateClause1[Select : Manifest, Domain : Manifest, RangeA : Manifest, RangeB : Manifest](
	project : Rep[Select => RangeA],
	aggregation : AGGREGATE_FUNCTION[Domain, RangeB],
	asDistinct: Boolean = false
) extends SELECT_TUPLED_AGGREGATE_CLAUSE_1[Select, Domain, RangeA, RangeB]
{

	def FROM (
		relation : Rep[Query[Domain]]
	) =
		aggregation match {
			case selfMaintained : AggregateFunctionSelfMaintained[Domain, RangeB] => {
				FromClause1[Select, Domain, (RangeA, RangeB)] (
					relation,
					SelectAggregateClause (
                        AggregateTupledFunctionSelfMaintained[Select, Domain, RangeA, RangeB, (RangeA, RangeB)] (
							selfMaintained.start,
							(x : Rep[(Domain, RangeB)]) => selfMaintained.added ((x._1, x._2)),
							(x : Rep[(Domain, RangeB)]) => selfMaintained.removed ((x._1, x._2)),
                            (x : Rep[(Domain, Domain, RangeB)]) => selfMaintained.updated ((x._1, x._2, x._3)),
							project,
							fun ( (x : Rep[(RangeA, RangeB, Domain)]) => (x._1, x._2) )
						),
						asDistinct
					)
				)
			}

			case notSelfMaintained : AggregateFunctionNotSelfMaintained[Domain, RangeB] => {
				FromClause1[Select, Domain, (RangeA, RangeB)] (
					relation,
					SelectAggregateClause (
						AggregateTupledFunctionNotSelfMaintained[Select, Domain, RangeA, RangeB, (RangeA, RangeB)] (
							notSelfMaintained.start,
							(x : Rep[(Domain, RangeB, Seq[Domain])]) =>
								notSelfMaintained.added ((x._1, x._2, x._3)),
							(x : Rep[(Domain, RangeB, Seq[Domain])]) =>
								notSelfMaintained.removed ((x._1, x._2, x._3)),
							(x : Rep[(Domain, Domain, RangeB, Seq[Domain])]) =>
								notSelfMaintained.updated ((x._1, x._2, x._3, x._4)),
							project,
							fun ( (x : Rep[(RangeA, RangeB, Domain)]) => (x._1, x._2) )
						),
						asDistinct
					)
				)
			}
		}


}
