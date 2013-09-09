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

import idb.syntax.iql._
import idb.syntax.iql.impl._

/**
 *
 * @author Ralf Mitschke
 *
 */

object ClauseToAlgebra {

    val IR = idb.syntax.iql.IR

    import IR._


    def apply[
		Select : Manifest,
		Domain <: GroupDomain : Manifest,
		GroupDomain: Manifest,
    	GroupRange <: Select : Manifest,
		Range : Manifest
	] (
        query: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause1 (relation, SelectClause (project, asDistinct)) =>
				distinct (
					projection (
						relation,
						project
					),
					asDistinct
                )

			case FromClause1 (
				relation,
				SelectAggregateClause1 (
					aggregate : AggregateFunction1[Domain@unchecked, Range@unchecked],
					asDistinct
				)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutGrouping(
						relation,
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					),
					asDistinct
				)



            case WhereClause1 (predicate, FromClause1 (relation, SelectClause (project, asDistinct))) =>
				distinct (
					projection (
						selection (
							relation,
							predicate
						),
						project
					),
					asDistinct
				)


            case GroupByClause1 (group, FromClause1 (relation, SelectClause (project, asDistinct))) =>
                distinct (
					projection(
                    	grouping (
                        	relation,
                       		group
                    	),
                    	project
					),
					asDistinct
                )

			case GroupByClause1 (
				group,
				FromClause1 (
					relation,
					SelectAggregateClause1 (
						aggregate : AggregateFunction1[Domain@unchecked, Range@unchecked],
						asDistinct)
				)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutConvert(
						relation,
						group,
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					),
					asDistinct
				)


            case GroupByClause1 (
				group,
				WhereClause1 (
					predicate,
					FromClause1 (
						relation,
						SelectClause (
							project,
							asDistinct
						)
					)
				)
			) =>
				distinct (
					projection(
						grouping (
							selection (
								relation,
								predicate
							),
							group
						),
						project
					),
					asDistinct
				)

			case GroupByClause1 (
				group,
				WhereClause1 (
					predicate,
					FromClause1 (
						relation,
						SelectAggregateClause1 (
							aggregate : AggregateFunction1[Domain@unchecked, Range@unchecked],
							asDistinct
						)
					)
				)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutConvert(
						selection (
							relation,
							predicate
						),
						group,
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					),
					asDistinct
				)
		}

 /*   def apply[
		Select: Manifest,
		DomainA <: GroupDomainA : Manifest,
		DomainB <: GroupDomainB : Manifest,
    	GroupDomainA: Manifest,
		GroupDomainB: Manifest,
    	GroupRange <: Select : Manifest,
		Range: Manifest
	] (
        query: IQL_QUERY_2[Select, DomainA, DomainB, GroupDomainA, GroupDomainB, GroupRange, Range]
    ): Rep[Query[Range]] =
        query match {
            case FromClause2 (
				relationA,
				relationB,
				SelectClause (
					project,
					asDistinct
				)
			) =>
				distinct (
					projection (
						crossProduct (
							relationA,
							relationB
						),
						project
					),
					asDistinct
				)

			case FromClause2 (
				relationA,
				relationB,
				SelectAggregateClause2 (
					aggregate : AggregateFunction2[DomainA@unchecked, DomainB@unchecked, Range@unchecked],
					asDistinct
				)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutGrouping(
						crossProduct (
							relationA,
							relationB
						),
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					),
					asDistinct
				)

            case WhereClause2 (
				predicate,
				FromClause2 (
					relationA,
					relationB,
					SelectClause (
						project,
						asDistinct
					)
				)
			) =>
				distinct (
					projection (
						selection (
							crossProduct (
								relationA,
								relationB
							),
							predicate
						),
						project
					),
					asDistinct
				)

			case WhereClause2 (
				predicate,
				FromClause2 (
					relationA,
					relationB,
					SelectAggregateClause2 (
						aggregate : AggregateFunction2[DomainA@unchecked, DomainB@unchecked, Range@unchecked],
						asDistinct
					)
				)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutGrouping (
						selection (
							crossProduct (
								relationA,
								relationB
							),
							predicate
						),
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					),
					asDistinct
				)

            case GroupByClause2 (
				group,
				FromClause2 (
					relationA,
					relationB,
					SelectClause (
						project,
						asDistinct
					)
				)
			) =>
				distinct (
					projection(
						grouping (
							crossProduct (
								relationA,
								relationB
							),
							group
						),
						project
					),
					asDistinct
				)

			case GroupByClause2 (
				group,
				FromClause2 (
					relationA,
					relationB,
					SelectAggregateClause2 (
						aggregate : AggregateFunction2[DomainA@unchecked, DomainB@unchecked, Range@unchecked],
						asDistinct
					)
				)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutConvert(
						crossProduct (
							relationA,
							relationB
						),
						group,
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					),
					asDistinct
				)

            case GroupByClause2 (
				group,
				WhereClause2 (
					predicate,
					FromClause2 (
						relationA,
						relationB,
						SelectClause (
							project,
							asDistinct
						)
					)
				)
			) =>

				distinct (
					projection(
						grouping (
							selection (
								crossProduct (
									relationA,
									relationB
								),
								predicate
							),
							group
						),
						project
					),
					asDistinct
				)

			case GroupByClause2 (
				group,
				WhereClause2 (
					predicate,
					FromClause2 (
						relationA,
						relationB,
						SelectAggregateClause2 (
							aggregate : AggregateFunction2[DomainA@unchecked, DomainB@unchecked, Range@unchecked],
							asDistinct
						)
					)
				)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutConvert(
						selection (
							crossProduct (
								relationA,
								relationB
							),
							predicate
						),
						group,
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					),
					asDistinct
				)

  		}   */

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
		implicit mSel : Manifest[Select],
		mDomA : Manifest[DomainA],
		mDomB : Manifest[DomainB],
		mGrDomA : Manifest[GroupDomainA],
		mGrDomB : Manifest[GroupDomainB],
		mGrRan : Manifest[GroupRange],
		mRan : Manifest[Range]
	): Rep[Query[Range]] =
		query match {
			case FromClause2 (
			relationA,
			relationB,
			SelectClause (
			project,
			asDistinct
			)
			) =>
				distinct (
					projection (
						crossProduct (
							relationA,
							relationB
						),
						project
					),
					asDistinct
				)

			case FromClause2 (
			relationA,
			relationB,
			SelectAggregateClause2 (
			aggregate : AggregateFunction2[DomainA@unchecked, DomainB@unchecked, Range@unchecked],
			asDistinct
			)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutGrouping(
						crossProduct (
							relationA,
							relationB
						),
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					)(
						implicitly[Manifest[(DomainA, DomainB)]],
						mRan
					),
					asDistinct
				)(
					mRan
				)

			case WhereClause2 (
			predicate,
			FromClause2 (
			relationA,
			relationB,
			SelectClause (
			project,
			asDistinct
			)
			)
			) =>
				distinct (
					projection (
						selection (
							crossProduct (
								relationA,
								relationB
							),
							predicate
						),
						project
					),
					asDistinct
				)

			case WhereClause2 (
			predicate,
			FromClause2 (
			relationA,
			relationB,
			SelectAggregateClause2 (
			aggregate : AggregateFunction2[DomainA@unchecked, DomainB@unchecked, Range@unchecked],
			asDistinct
			)
			)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutGrouping (
						selection (
							crossProduct (
								relationA,
								relationB
							),
							predicate
						),
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					)(
						implicitly[Manifest[(DomainA, DomainB)]],
						mRan
					),
					asDistinct
				)(
					mRan
				)

			case GroupByClause2 (
			group,
			FromClause2 (
			relationA,
			relationB,
			SelectClause (
			project,
			asDistinct
			)
			)
			) =>
				distinct (
					projection(
						grouping (
							crossProduct (
								relationA,
								relationB
							),
							group
						),
						project
					),
					asDistinct
				)

			case GroupByClause2 (
			group,
			FromClause2 (
			relationA,
			relationB,
			SelectAggregateClause2 (
			aggregate : AggregateFunction2[DomainA@unchecked, DomainB@unchecked, Range@unchecked],
			asDistinct
			)
			)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutConvert(
						crossProduct (
							relationA,
							relationB
						),
						group,
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					),
					asDistinct
				)

			case GroupByClause2 (
			group,
			WhereClause2 (
			predicate,
			FromClause2 (
			relationA,
			relationB,
			SelectClause (
			project,
			asDistinct
			)
			)
			)
			) =>

				distinct (
					projection(
						grouping (
							selection (
								crossProduct (
									relationA,
									relationB
								),
								predicate
							),
							group
						),
						project
					),
					asDistinct
				)

			case GroupByClause2 (
			group,
			WhereClause2 (
			predicate,
			FromClause2 (
			relationA,
			relationB,
			SelectAggregateClause2 (
			aggregate : AggregateFunction2[DomainA@unchecked, DomainB@unchecked, Range@unchecked],
			asDistinct
			)
			)
			)
			) =>
				distinct (
					aggregationSelfMaintainedWithoutConvert(
						selection (
							crossProduct (
								relationA,
								relationB
							),
							predicate
						),
						group,
						aggregate.start,
						aggregate.added,
						aggregate.removed,
						aggregate.updated
					),
					asDistinct
				)

		}


	def apply[
		Select: Manifest,
		DomainA <: GroupDomainA : Manifest,
		DomainB <: GroupDomainB : Manifest,
		DomainC <: GroupDomainC : Manifest,
		GroupDomainA : Manifest,
		GroupDomainB : Manifest,
		GroupDomainC : Manifest,
		GroupRange <: Select : Manifest,
		Range: Manifest
	] (
		query: IQL_QUERY_3[Select, DomainA, DomainB, DomainC, GroupDomainA, GroupDomainB, GroupDomainC, GroupRange, Range]
	): Rep[Query[Range]] =
        query match {

			case FromClause3 (
				relationA : Rep[Query[DomainA]@unchecked],
				relationB : Rep[Query[DomainB]@unchecked],
				relationC : Rep[Query[DomainC]@unchecked],
				select
			) =>
				applySelectClause (
					crossProduct (
						relationA,
						relationB,
						relationC
					),
					select
				)


			case WhereClause3 (predicate,  FromClause3 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], select)) =>
				applySelectClause (
					selection (
						crossProduct (
							relationA,
							relationB,
							relationC
						),
						predicate
					),
					select
				)


			case GroupByClause3 (group,  FromClause3 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], select)) =>
				applySelectClause (
					grouping (
						crossProduct (
							relationA,
							relationB,
							relationC
						),
						group
					),
					select
				)

			case GroupByClause3 (group, WhereClause3 (predicate,  FromClause3 (relationA : Rep[Query[DomainA]@unchecked], relationB : Rep[Query[DomainB]@unchecked], relationC : Rep[Query[DomainC]@unchecked], select))) =>
				applySelectClause (
					grouping (
						selection (
							crossProduct (
								relationA,
								relationB,
								relationC
							),
							predicate
						),
						group
					),
					select
				)
        }

	def apply[
		Select: Manifest,
		DomainA <: GroupDomainA : Manifest,
		DomainB <: GroupDomainB : Manifest,
		DomainC <: GroupDomainC : Manifest,
		DomainD <: GroupDomainD : Manifest,
		GroupDomainA : Manifest,
		GroupDomainB : Manifest,
		GroupDomainC : Manifest,
		GroupDomainD : Manifest,
		GroupRange <: Select : Manifest,
		Range: Manifest
	] (
		  query: IQL_QUERY_4[Select, DomainA, DomainB, DomainC, DomainD, GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupRange, Range]
		  ): Rep[Query[Range]] =
        query match {
			case FromClause4 (
				relationA : Rep[Query[DomainA]@unchecked],
				relationB : Rep[Query[DomainB]@unchecked],
				relationC : Rep[Query[DomainC]@unchecked],
				relationD : Rep[Query[DomainD]@unchecked],
				select
			) =>
				applySelectClause (
					crossProduct (
						relationA,
						relationB,
						relationC,
						relationD
					),
					select
				)


			case WhereClause4 (
				predicate,
				FromClause4 (
					relationA : Rep[Query[DomainA]@unchecked],
					relationB : Rep[Query[DomainB]@unchecked],
					relationC : Rep[Query[DomainC]@unchecked],
					relationD : Rep[Query[DomainD]@unchecked],
					select
				)
			) =>
				applySelectClause (
					selection (
						crossProduct (
							relationA,
							relationB,
							relationC,
							relationD
						),
						predicate
					),
					select
				)


			case GroupByClause4 (
				group,
				FromClause4 (
					relationA : Rep[Query[DomainA]@unchecked],
					relationB : Rep[Query[DomainB]@unchecked],
					relationC : Rep[Query[DomainC]@unchecked],
					relationD : Rep[Query[DomainD]@unchecked],
					select
				)
			) =>
				applySelectClause (
					grouping (
						crossProduct (
							relationA,
							relationB,
							relationC,
							relationD
						),
						group
					),
					select
				)

			case GroupByClause4 (
				group,
				WhereClause4 (
					predicate,
					FromClause4 (
						relationA : Rep[Query[DomainA]@unchecked],
						relationB : Rep[Query[DomainB]@unchecked],
						relationC : Rep[Query[DomainC]@unchecked],
						relationD : Rep[Query[DomainD]@unchecked],
						select
					)
				)
			) =>
				applySelectClause (
					grouping (
						selection (
							crossProduct (
								relationA,
								relationB,
								relationC,
								relationD
							),
							predicate
						),
						group
					),
					select
				)
        }

	def apply[
		Select: Manifest,
		DomainA <: GroupDomainA : Manifest,
		DomainB <: GroupDomainB : Manifest,
		DomainC <: GroupDomainC : Manifest,
		DomainD <: GroupDomainD : Manifest,
		DomainE <: GroupDomainE : Manifest,
		GroupDomainA : Manifest,
		GroupDomainB : Manifest,
		GroupDomainC : Manifest,
		GroupDomainD : Manifest,
		GroupDomainE : Manifest,
		GroupRange <: Select : Manifest,
		Range: Manifest
	] (
		  query: IQL_QUERY_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, GroupDomainA, GroupDomainB, GroupDomainC, GroupDomainD, GroupDomainE, GroupRange, Range]
		  ): Rep[Query[Range]] =
		query match {
			case FromClause5 (
				relationA : Rep[Query[DomainA]@unchecked],
				relationB : Rep[Query[DomainB]@unchecked],
				relationC : Rep[Query[DomainC]@unchecked],
				relationD : Rep[Query[DomainD]@unchecked],
				relationE : Rep[Query[DomainD]@unchecked],
				select
			) =>
				applySelectClause (
					crossProduct (
						relationA,
						relationB,
						relationC,
						relationD,
						relationE
					),
					select
				)


			case WhereClause5 (
				predicate,
				FromClause5 (
					relationA : Rep[Query[DomainA]@unchecked],
					relationB : Rep[Query[DomainB]@unchecked],
					relationC : Rep[Query[DomainC]@unchecked],
					relationD : Rep[Query[DomainD]@unchecked],
					relationE : Rep[Query[DomainE]@unchecked],
					select
				)
			) =>
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
					),
					select
				)


			case GroupByClause5 (
				group,
				FromClause5 (
					relationA : Rep[Query[DomainA]@unchecked],
					relationB : Rep[Query[DomainB]@unchecked],
					relationC : Rep[Query[DomainC]@unchecked],
					relationD : Rep[Query[DomainD]@unchecked],
					relationE : Rep[Query[DomainE]@unchecked],
					select
				)
			) =>
				applySelectClause (
					grouping (
						crossProduct (
							relationA,
							relationB,
							relationC,
							relationD,
							relationE
						),
						group
					),
					select
				)

			case GroupByClause5 (
				group,
				WhereClause5 (
					predicate,
					FromClause5 (
						relationA : Rep[Query[DomainA]@unchecked],
						relationB : Rep[Query[DomainB]@unchecked],
						relationC : Rep[Query[DomainC]@unchecked],
						relationD : Rep[Query[DomainD]@unchecked],
						relationE : Rep[Query[DomainE]@unchecked],
						select
					)
				)
			) =>
				applySelectClause (
					grouping (
						selection (
							crossProduct (
								relationA,
								relationB,
								relationC,
								relationD,
								relationE
							),
							predicate
						),
						group
					),
					select
				)
		}

    private def distinct[Domain: Manifest] (query: Rep[Query[Domain]], asDistinct: Boolean): Rep[Query[Domain]] = {
        asDistinct match {
            case true => duplicateElimination (query)
            case false => query
        }
    }

    private def applySelectClause[Select: Manifest, Domain <: Select : Manifest, Range: Manifest] (
        relation: Rep[Query[Domain]],
        select: SELECT_CLAUSE[Select, Range]
    ): Rep[Query[Range]] = {
        select match {
         /*   case SelectAggregateClause1 (aggregation : AggregateFunction[Domain@unchecked, Range@unchecked], asDistinct) =>
                distinct (
                    aggregationSelfMaintainedWithoutGrouping (
                        relation,
                        aggregation.start,
                        aggregation.added,
                        aggregation.removed,
                        aggregation.updated
                    ),
                    asDistinct
                )          */
            case SelectClause (project, asDistinct) =>
                distinct (
                    projection (
                        relation,
                        project
                    ),
                    asDistinct
                )
        }
    }


}
