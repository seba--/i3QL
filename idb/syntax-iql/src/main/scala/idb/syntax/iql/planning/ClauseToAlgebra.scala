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
package idb.syntax.iql.planning

//import idb.syntax.iql.IR._

import idb.query.QueryEnvironment
import idb.syntax.iql._
import idb.syntax.iql.impl._


/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 *
 */

object ClauseToAlgebra {

    val IR = idb.syntax.iql.IR
    import IR._

    def apply[
		Select,
		Domain <: GroupDomain,
		GroupDomain,
		GroupRange <: Select,
		Range
    ] (
        query: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range]
    )(
        implicit mSel: Manifest[Select],
        mDom: Manifest[Domain],
        mGrDom: Manifest[GroupDomain],
        mGrRan: Manifest[GroupRange],
        mRan: Manifest[Range],
		queryEnvironment : QueryEnvironment
    ): Rep[Query[Range]] =
        query match {
            case FromClause1 (relation : Rep[Query[Domain]@unchecked], select) =>
                applySelectClause (
                    relation,
                    select
                )

            case WhereClause1 (predicate, FromClause1 (relation : Rep[Query[Domain]@unchecked], select)) =>
                applySelectClause (
                    selection (
                        relation,
                        predicate
                    ),
                    select
                )

            case GroupByClause1 (group, FromClause1 (relation : Rep[Query[Domain]@unchecked], select)) =>
                applyGroupedSelectClause (
                    relation,
                    group,
                    select
                )

            case GroupByClause1 (group, WhereClause1 (predicate, FromClause1 (relation : Rep[Query[Domain]@unchecked], select))) =>
                applyGroupedSelectClause (
                    selection (
                        relation,
                        predicate
                    ),
                    group,
                    select
                )
        }


    def apply[
		Select,
		DomainA <: GroupDomainA,
		DomainB <: GroupDomainB,
		GroupDomainA,
		GroupDomainB,
		GroupRange <: Select,
		Range
    ] (
        query: IQL_QUERY_2[Select, DomainA, DomainB, GroupDomainA, GroupDomainB, GroupRange, Range]
    )(
        implicit mSel: Manifest[Select],
        mDomA: Manifest[DomainA],
        mDomB: Manifest[DomainB],
        mGrDomA: Manifest[GroupDomainA],
        mGrDomB: Manifest[GroupDomainB],
        mGrRan: Manifest[GroupRange],
        mRan: Manifest[Range],
		queryEnvironment : QueryEnvironment
	): Rep[Query[Range]] =
        query match {
            case FromClause2 (relationA: Rep[Query[DomainA]@unchecked], relationB: Rep[Query[DomainB]@unchecked], select) =>
                applySelectClause (
                    crossProduct (
                        relationA,
                        relationB
                    ),
                    select
                )(mSel, manifest[(DomainA, DomainB)], mRan, queryEnvironment)

            case WhereClause2 (predicate, FromClause2 (relationA: Rep[Query[DomainA]@unchecked], relationB: Rep[Query[DomainB]@unchecked], select)) =>
                applySelectClause (
                    selection (
                        crossProduct (
                            relationA,
                            relationB
                        ),
                        predicate
                    ) (manifest[(DomainA, DomainB)], queryEnvironment),
                    select
                ) (mSel, manifest[(DomainA, DomainB)], mRan, queryEnvironment)

            case GroupByClause2 (group : Rep[((GroupDomainA, GroupDomainB)) => GroupRange], FromClause2 (relationA: Rep[Query[DomainA]@unchecked], relationB: Rep[Query[DomainB]@unchecked], select)) =>
                applyGroupedSelectClause (
                    crossProduct (
                        relationA,
                        relationB
                    ),
                    group,
                    select
                ) (mSel, manifest[(DomainA, DomainB)], manifest[(GroupDomainA, GroupDomainB)], mGrRan, mRan, queryEnvironment)

            case GroupByClause2 (group, WhereClause2 (predicate, FromClause2 (relationA: Rep[Query[DomainA]@unchecked], relationB: Rep[Query[DomainB]@unchecked], select))) =>
                applyGroupedSelectClause (
                    selection (
                        crossProduct (
                            relationA,
                            relationB
                        ),
                        predicate
                    ) (manifest[(DomainA, DomainB)], queryEnvironment),
                    group,
                    select
                )  (mSel, manifest[(DomainA, DomainB)], manifest[(GroupDomainA, GroupDomainB)], mGrRan, mRan, queryEnvironment)
        }


    def apply[
		Select,
		DomainA <: GroupDomainA,
		DomainB <: GroupDomainB,
		DomainC <: GroupDomainC,
		GroupDomainA,
		GroupDomainB,
		GroupDomainC,
		GroupRange <: Select,
		Range
    ] (
        query: IQL_QUERY_3[Select, DomainA, DomainB, DomainC, GroupDomainA, GroupDomainB, GroupDomainC, GroupRange,
            Range]
    )(
		implicit mSel: Manifest[Select],
		mDomA: Manifest[DomainA],
		mDomB: Manifest[DomainB],
		mDomC : Manifest[DomainC],
		mGrDomA: Manifest[GroupDomainA],
		mGrDomB: Manifest[GroupDomainB],
		mGrDomC : Manifest[GroupDomainC],
		mGrRan: Manifest[GroupRange],
		mRan: Manifest[Range],
		queryEnvironment : QueryEnvironment
	): Rep[Query[Range]] =
        query match {
            case FromClause3 (relationA: Rep[Query[DomainA]@unchecked], relationB: Rep[Query[DomainB]@unchecked],
            relationC: Rep[Query[DomainC]@unchecked], select) =>
                applySelectClause (
                    crossProduct (
                        relationA,
                        relationB,
                        relationC
                    ),
                    select
                ) (mSel, manifest[(DomainA, DomainB, DomainC)], mRan, queryEnvironment)

            case WhereClause3 (predicate,
            FromClause3 (relationA: Rep[Query[DomainA]@unchecked], relationB: Rep[Query[DomainB]@unchecked],
            relationC: Rep[Query[DomainC]@unchecked], select)) =>
                applySelectClause (
                    selection (
                        crossProduct (
                            relationA,
                            relationB,
                            relationC
                        ),
                        predicate
                    ) (manifest[(DomainA, DomainB, DomainC)], queryEnvironment),
                    select
                ) (mSel, manifest[(DomainA, DomainB, DomainC)], mRan, queryEnvironment)

            case GroupByClause3 (group,
            FromClause3 (relationA: Rep[Query[DomainA]@unchecked], relationB: Rep[Query[DomainB]@unchecked],
            relationC: Rep[Query[DomainC]@unchecked], select)) =>
                applyGroupedSelectClause (
                    crossProduct (
                        relationA,
                        relationB,
                        relationC
                    ),
                    group,
                    select
                ) (mSel, manifest[(DomainA, DomainB, DomainC)], manifest[(GroupDomainA, GroupDomainB, GroupDomainC)], mGrRan, mRan, queryEnvironment)

            case GroupByClause3 (group, WhereClause3 (predicate,
            FromClause3 (relationA: Rep[Query[DomainA]@unchecked], relationB: Rep[Query[DomainB]@unchecked],
            relationC: Rep[Query[DomainC]@unchecked], select))) =>
                applyGroupedSelectClause (
                    selection (
                        crossProduct (
                            relationA,
                            relationB,
                            relationC
                        ),
                        predicate
                    ) (manifest[(DomainA, DomainB, DomainC)], queryEnvironment),
                    group,
                    select
                ) (mSel, manifest[(DomainA, DomainB, DomainC)], manifest[(GroupDomainA, GroupDomainB, GroupDomainC)], mGrRan, mRan, queryEnvironment)
        }


	def apply[
		Select,
		DomainA <: GroupDomainA,
		DomainB <: GroupDomainB,
		DomainC <: GroupDomainC,
		DomainD <: GroupDomainD,
		GroupDomainA,
		GroupDomainB,
		GroupDomainC,
		GroupDomainD,
		GroupRange <: Select,
		Range
	] (
		  query: IQL_QUERY_4[Select, DomainA, DomainB, DomainC, DomainD, GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupRange, Range]
  	)(
		implicit mSel: Manifest[Select],
		mDomA: Manifest[DomainA],
		mDomB: Manifest[DomainB],
		mDomC : Manifest[DomainC],
		mDomD : Manifest[DomainD],
		mGrDomA: Manifest[GroupDomainA],
		mGrDomB: Manifest[GroupDomainB],
		mGrDomC : Manifest[GroupDomainC],
		mGrDomD: Manifest[GroupDomainD],
		mGrRan: Manifest[GroupRange],
		mRan: Manifest[Range],
		queryEnvironment : QueryEnvironment
	): Rep[Query[Range]] =
		query match {
			case FromClause4 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], relationD : Rep[Query[DomainD]@unchecked], select) =>
				applySelectClause (
					crossProduct (
						relationA,
						relationB,
						relationC,
						relationD
					),
					select
				)(mSel, manifest[(DomainA, DomainB, DomainC, DomainD)], mRan, queryEnvironment)

			case WhereClause4 (predicate, FromClause4 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], relationD : Rep[Query[DomainD]@unchecked], select)) =>
				applySelectClause (
					selection (
						crossProduct (
							relationA,
							relationB,
							relationC,
							relationD
						),
						predicate
					) (manifest[(DomainA, DomainB, DomainC, DomainD)], queryEnvironment),
					select
				)(mSel, manifest[(DomainA, DomainB, DomainC, DomainD)], mRan, queryEnvironment)

			case GroupByClause4 (group, FromClause4 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], relationD : Rep[Query[DomainD]@unchecked], select)) =>
				applyGroupedSelectClause(
					crossProduct (
						relationA,
						relationB,
						relationC,
						relationD
					),
					group,
					select
				) (mSel, manifest[(DomainA, DomainB, DomainC, DomainD)], manifest[(GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD)], mGrRan, mRan, queryEnvironment)

			case GroupByClause4 (group, WhereClause4 (predicate, FromClause4 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], relationD : Rep[Query[DomainD]@unchecked], select))) =>
				applyGroupedSelectClause(
					selection (
						crossProduct (
							relationA,
							relationB,
							relationC,
							relationD
						),
						predicate
					) (manifest[(DomainA, DomainB, DomainC, DomainD)], queryEnvironment),
					group,
					select
				) (mSel, manifest[(DomainA, DomainB, DomainC, DomainD)], manifest[(GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD)], mGrRan, mRan, queryEnvironment)
		}

	def apply[
		Select,
		DomainA <: GroupDomainA,
		DomainB <: GroupDomainB,
		DomainC <: GroupDomainC,
		DomainD <: GroupDomainD,
		DomainE <: GroupDomainE,
		GroupDomainA,
		GroupDomainB,
		GroupDomainC,
		GroupDomainD,
		GroupDomainE,
		GroupRange <: Select,
		Range
	] (
		query: IQL_QUERY_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupDomainE, GroupRange, Range]
	)(
		implicit mSel: Manifest[Select],
		mDomA: Manifest[DomainA],
		mDomB: Manifest[DomainB],
		mDomC : Manifest[DomainC],
		mDomD : Manifest[DomainD],
		mDomE : Manifest[DomainE],
		mGrDomA: Manifest[GroupDomainA],
		mGrDomB: Manifest[GroupDomainB],
		mGrDomC : Manifest[GroupDomainC],
		mGrDomD: Manifest[GroupDomainD],
		mGrDomE : Manifest[GroupDomainE],
		mGrRan: Manifest[GroupRange],
		mRan: Manifest[Range],
		queryEnvironment : QueryEnvironment
	): Rep[Query[Range]] =
		query match {
			case FromClause5 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], relationD : Rep[Query[DomainD]@unchecked], relationE : Rep[Query[DomainE]@unchecked], select) =>
				applySelectClause (
					crossProduct (
						relationA,
						relationB,
						relationC,
						relationD,
						relationE
					),
					select
				)(mSel, manifest[(DomainA, DomainB, DomainC, DomainD, DomainE)], mRan, queryEnvironment)

			case WhereClause5 (predicate, FromClause5 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], relationD : Rep[Query[DomainD]@unchecked], relationE : Rep[Query[DomainE]@unchecked], select)) =>
				applySelectClause (
					selection (
						crossProduct (
							relationA,
							relationB,
							relationC,
							relationD,
							relationE
						),
						predicate
					)(manifest[(DomainA, DomainB, DomainC, DomainD, DomainE)], queryEnvironment),
					select
				)(mSel, manifest[(DomainA, DomainB, DomainC, DomainD, DomainE)], mRan, queryEnvironment)

			case GroupByClause5 (group, FromClause5 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], relationD : Rep[Query[DomainD]@unchecked], relationE : Rep[Query[DomainE]@unchecked], select)) =>
				applyGroupedSelectClause(
					crossProduct (
						relationA,
						relationB,
						relationC,
						relationD,
						relationE
					),
					group,
					select
				)(mSel, manifest[(DomainA, DomainB, DomainC, DomainD, DomainE)], manifest[(GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupDomainE)], mGrRan, mRan, queryEnvironment)

			case GroupByClause5 (group, WhereClause5 (predicate, FromClause5 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], relationD : Rep[Query[DomainD]@unchecked], relationE : Rep[Query[DomainE]@unchecked], select))) =>
				applyGroupedSelectClause(
					selection (
						crossProduct (
							relationA,
							relationB,
							relationC,
							relationD,
							relationE
						),
						predicate
					)(manifest[(DomainA, DomainB, DomainC, DomainD, DomainE)], queryEnvironment),
					group,
					select
				)(mSel, manifest[(DomainA, DomainB, DomainC, DomainD, DomainE)], manifest[(GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupDomainE)], mGrRan, mRan, queryEnvironment)
		}

    private def distinct[Domain: Manifest] (query: Rep[Query[Domain]], asDistinct: Boolean)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] = {
        asDistinct match {
            case true => duplicateElimination (query)
            case false => query
        }
    }


    private def applySelectClause[Select: Manifest, Domain <: Select : Manifest, Range: Manifest] (
        relation: Rep[Query[Domain]],
        select: SELECT_CLAUSE[Select, Range]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] = {
        select match {
            case SelectAggregateClause (aggregation : AggregateFunctionSelfMaintained[Domain@unchecked, Range@unchecked], asDistinct) =>
                distinct (
                    aggregationSelfMaintained[Domain , Boolean, Boolean, Range, Range](
                        relation,
	                    grouping = (x : Rep[Domain]) => __anythingAsUnit(true),
                        start = aggregation.start,
                        added = aggregation.added,
                        removed = aggregation.removed,
                        updated = aggregation.updated,
	                    convertKey = (x : Rep[Boolean]) => x,
	                    convert = (t : Rep[(Boolean, Range, Domain)]) => t._2
                    ),
                    asDistinct
                )

			case SelectAggregateClause (aggregation : AggregateFunctionNotSelfMaintained[Domain@unchecked, Range@unchecked], asDistinct) =>
				distinct (
					aggregationNotSelfMaintained[Domain , Boolean, Boolean, Range, Range] (
						relation,
						grouping = (x : Rep[Domain]) => __anythingAsUnit(true),
						start = aggregation.start,
						added = aggregation.added,
						removed = aggregation.removed,
						updated = aggregation.updated,
						convertKey = (x : Rep[Boolean]) => x,
						convert = (t : Rep[(Boolean, Range, Domain)]) => t._2
					),
					asDistinct
				)

			case SelectProjectionClause (project, asDistinct) =>
                distinct (
                    projection (
                        relation,
                        project
                    ),
                    asDistinct
                )
        }
    }

	private def applyGroupedSelectClause[
		Select : Manifest,
		Domain <: GroupDomain : Manifest,
		GroupDomain: Manifest,
		GroupRange <: Select : Manifest,
		Range : Manifest
	] (
		relation: Rep[Query[Domain]],
		group : Rep[GroupDomain => GroupRange],
		select: SELECT_CLAUSE[Select, Range]
	)(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] = {
		select match {
			case SelectAggregateClause (aggregation : AggregateFunctionSelfMaintained[Domain@unchecked, Range@unchecked], asDistinct) =>
				distinct (
					aggregationSelfMaintained[Domain , GroupRange, GroupRange, Range, Range](
						relation,
						grouping = group,
						start = aggregation.start,
						added = aggregation.added,
						removed = aggregation.removed,
						updated = aggregation.updated,
						convertKey = (x : Rep[GroupRange]) => x,
						convert = (t : Rep[(GroupRange, Range, Domain)]) => t._2
					),
					asDistinct
				)

			case SelectAggregateClause (aggregation : AggregateTupledFunctionSelfMaintained[Select@unchecked, Domain@unchecked, _, _, Range@unchecked], asDistinct) =>
				distinct (
					aggregationSelfMaintained (
						relation,
						group,
						aggregation.start,
						aggregation.added,
						aggregation.removed,
						aggregation.updated,
						aggregation.project,
						aggregation.convert
					),
					asDistinct
				)

			case SelectAggregateClause (aggregation : AggregateFunctionNotSelfMaintained[Domain@unchecked, Range@unchecked], asDistinct) =>
				distinct (
					aggregationNotSelfMaintained[Domain , GroupRange, GroupRange, Range, Range] (
						relation,
						grouping = group,
						start = aggregation.start,
						added = aggregation.added,
						removed = aggregation.removed,
						updated = aggregation.updated,
						convertKey = (x : Rep[GroupRange]) => x,
						convert = (t : Rep[(GroupRange, Range, Domain)]) => t._2
					),
					asDistinct
				)

			case SelectAggregateClause (aggregation : AggregateTupledFunctionNotSelfMaintained[Select@unchecked, Domain@unchecked, _, _, Range@unchecked], asDistinct) =>
				distinct (
					aggregationNotSelfMaintained (
						relation,
						group,
						aggregation.start,
						aggregation.added,
						aggregation.removed,
						aggregation.updated,
						aggregation.project,
						aggregation.convert
					),
					asDistinct
				)

			case SelectProjectionClause (project, asDistinct) =>
				distinct (
					projection (
						aggregationSelfMaintained (
							relation,
							grouping = group,
							start = true,
							added = (t : Rep[(Domain, Boolean)]) => __anythingAsUnit(true),
							removed = (t : Rep[(Domain, Boolean)]) => __anythingAsUnit(true),
							updated = (t : Rep[(Domain, Domain, Boolean)]) => __anythingAsUnit(true),
							convertKey = (x : Rep[GroupRange]) => x,
							convert = (t : Rep[(GroupRange, Boolean, Domain)]) => t._1
						),
						project
					),
					asDistinct
				)
		}
	}



}
