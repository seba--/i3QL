/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package idb.syntax.iql.impl

import idb.syntax.iql._
import idb.syntax.iql.IR._


/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 */
case class SelectTupledAggregateClauseStar[Select : Manifest, RangeA : Manifest, RangeB : Manifest] (
	project : Rep[Select => RangeA],
	aggregation : AGGREGATE_FUNCTION_STAR[RangeB],
	asDistinct: Boolean = false
)
    extends SELECT_TUPLED_AGGREGATE_CLAUSE_STAR[Select, RangeA, RangeB]
{
    def FROM[Domain : Manifest] (
        relation: Rep[Query[Domain]]
    ): FROM_CLAUSE_1[Select, Domain, (RangeA, RangeB)]
		with CAN_GROUP_CLAUSE_1[Select, Domain, (RangeA, RangeB)] =
		aggregation.getAggregateFunction[Domain] match {
			case selfMaintained : AggregateFunctionSelfMaintained[Domain, RangeB] => {
				FromClause1[Select, Domain, (RangeA, RangeB)] (
					relation,
					SelectAggregateClause (
						AggregateTupledFunctionSelfMaintained[Select, Domain, RangeA, RangeB, (RangeA, RangeB)] (
							selfMaintained.start,
							(x : Rep[(Domain, RangeB)]) =>
								selfMaintained.added ((x._1, x._2)),
							(x : Rep[(Domain, RangeB)]) =>
								selfMaintained.removed ((x._1, x._2)),
							(x : Rep[(Domain, Domain, RangeB)]) =>
								selfMaintained.updated ((x._1, x._2, x._3)),
							project,
							fun ( (x : Rep[(RangeA, RangeB)]) => x )
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
							fun ( (x : Rep[(RangeA, RangeB)]) => x )
						),
						asDistinct
					)
				)
			}
		}

    def FROM[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    ): FROM_CLAUSE_2[Select, DomainA, DomainB, (RangeA, RangeB)]
		with CAN_GROUP_CLAUSE_2[Select, DomainA, DomainB, (RangeA, RangeB)] =
		aggregation.getAggregateFunction[(DomainA, DomainB)] match {
			case selfMaintained : AggregateFunctionSelfMaintained[(DomainA, DomainB), RangeB] => {
				FromClause2[Select, DomainA, DomainB, (RangeA, RangeB)] (
					relationA,
					relationB,
					SelectAggregateClause (
						AggregateTupledFunctionSelfMaintained[Select, (DomainA, DomainB), RangeA, RangeB, (RangeA, RangeB)] (
							selfMaintained.start,
							(x : Rep[((DomainA, DomainB), RangeB)]) =>
								selfMaintained.added ((x._1, x._2)),
							(x : Rep[((DomainA, DomainB), RangeB)]) =>
								selfMaintained.removed ((x._1, x._2)),
							(x : Rep[((DomainA, DomainB), (DomainA, DomainB), RangeB)]) =>
								selfMaintained.updated ((x._1, x._2, x._3)),
							project,
							fun ( (x : Rep[(RangeA, RangeB)]) => x )
						),
						asDistinct
					)
				)
			}

			case notSelfMaintained : AggregateFunctionNotSelfMaintained[(DomainA, DomainB), RangeB] => {
				FromClause2[Select, DomainA, DomainB, (RangeA, RangeB)] (
					relationA,
					relationB,
					SelectAggregateClause (
						AggregateTupledFunctionNotSelfMaintained[Select, (DomainA, DomainB), RangeA, RangeB, (RangeA, RangeB)] (
							notSelfMaintained.start,
							(x : Rep[((DomainA, DomainB), RangeB, Seq[(DomainA, DomainB)])]) =>
								notSelfMaintained.added ((x._1, x._2, x._3)),
							(x : Rep[((DomainA, DomainB), RangeB, Seq[(DomainA, DomainB)])]) =>
								notSelfMaintained.removed ((x._1, x._2, x._3)),
							(x : Rep[((DomainA, DomainB), (DomainA, DomainB), RangeB, Seq[(DomainA, DomainB)])]) =>
								notSelfMaintained.updated ((x._1, x._2, x._3, x._4)),
							project,
							fun ( (x : Rep[(RangeA, RangeB)]) => x )
						),
						asDistinct
					)
				)
			}
		}


	def FROM[DomainA : Manifest, DomainB : Manifest, DomainC : Manifest] (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]]
	): FROM_CLAUSE_3[Select, DomainA, DomainB, DomainC, (RangeA, RangeB)]
		with CAN_GROUP_CLAUSE_3[Select, DomainA, DomainB, DomainC, (RangeA, RangeB)] =
		aggregation.getAggregateFunction[(DomainA, DomainB, DomainC)] match {
			case selfMaintained : AggregateFunctionSelfMaintained[(DomainA, DomainB, DomainC), RangeB] => {
				FromClause3[Select, DomainA, DomainB, DomainC, (RangeA, RangeB)] (
					relationA,
					relationB,
					relationC,
					SelectAggregateClause (
						AggregateTupledFunctionSelfMaintained[Select, (DomainA, DomainB, DomainC), RangeA, RangeB, (RangeA, RangeB)] (
							selfMaintained.start,
							(x : Rep[((DomainA, DomainB, DomainC), RangeB)]) =>
								selfMaintained.added ((x._1, x._2)),
							(x : Rep[((DomainA, DomainB, DomainC), RangeB)]) =>
								selfMaintained.removed ((x._1, x._2)),
							(x : Rep[((DomainA, DomainB, DomainC), (DomainA, DomainB, DomainC), RangeB)]) =>
								selfMaintained.updated ((x._1, x._2, x._3)),
							project,
							fun ( (x : Rep[(RangeA, RangeB)]) => x )
						),
						asDistinct
					)
				)
			}

			case notSelfMaintained : AggregateFunctionNotSelfMaintained[(DomainA, DomainB, DomainC), RangeB] => {
				FromClause3[Select, DomainA, DomainB, DomainC, (RangeA, RangeB)] (
					relationA,
					relationB,
					relationC,
					SelectAggregateClause (
						AggregateTupledFunctionNotSelfMaintained[Select, (DomainA, DomainB, DomainC), RangeA, RangeB, (RangeA, RangeB)] (
							notSelfMaintained.start,
							(x : Rep[((DomainA, DomainB, DomainC), RangeB, Seq[(DomainA, DomainB, DomainC)])]) =>
								notSelfMaintained.added ((x._1, x._2, x._3)),
							(x : Rep[((DomainA, DomainB, DomainC), RangeB, Seq[(DomainA, DomainB, DomainC)])]) =>
								notSelfMaintained.removed ((x._1, x._2, x._3)),
							(x : Rep[((DomainA, DomainB, DomainC), (DomainA, DomainB, DomainC), RangeB, Seq[(DomainA, DomainB, DomainC)])]) =>
								notSelfMaintained.updated ((x._1, x._2, x._3, x._4)),
							project,
							fun ( (x : Rep[(RangeA, RangeB)]) => x )
						),
						asDistinct
					)
				)
			}
		}

	def FROM[DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest] (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]],
		relationD : Rep[Query[DomainD]]
	): FROM_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, (RangeA, RangeB)]
		with CAN_GROUP_CLAUSE_4[Select, DomainA, DomainB, DomainC, DomainD, (RangeA, RangeB)] =
		aggregation.getAggregateFunction[(DomainA, DomainB, DomainC, DomainD)] match {
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

	def FROM[DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, DomainD : Manifest, DomainE : Manifest] (
		relationA : Rep[Query[DomainA]],
		relationB : Rep[Query[DomainB]],
		relationC : Rep[Query[DomainC]],
		relationD : Rep[Query[DomainD]],
		relationE : Rep[Query[DomainE]]
 	): FROM_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, (RangeA, RangeB)]
		with CAN_GROUP_CLAUSE_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, (RangeA, RangeB)] =
		aggregation.getAggregateFunction[(DomainA, DomainB, DomainC, DomainD, DomainE)] match {
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
