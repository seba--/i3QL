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
package idb.syntax.iql


import idb.syntax.iql.IR._
import idb.syntax.iql.impl._

/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 */
object SELECT
{

    def apply[Select : Manifest, Range : Manifest] (
        projection: Rep[Select] => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE[Select, Range]  =
        SelectProjectionClause (projection)


    def apply[SelectA : Manifest, SelectB : Manifest, Range: Manifest] (
        projection: (Rep[SelectA], Rep[SelectB]) => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE[(SelectA, SelectB), Range] =
        SelectProjectionClause (fun (projection))


    def apply[SelectA : Manifest, SelectB : Manifest, SelectC : Manifest, Range: Manifest] (
        projection: (Rep[SelectA], Rep[SelectB], Rep[SelectC]) => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE[(SelectA, SelectB, SelectC), Range] =
        SelectProjectionClause (fun (projection))


    def apply[SelectA : Manifest, SelectB : Manifest, SelectC : Manifest, SelectD : Manifest, Range : Manifest] (
        projection: (Rep[SelectA], Rep[SelectB], Rep[SelectC], Rep[SelectD]) => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE[(SelectA, SelectB, SelectC, SelectD), Range] =
        SelectProjectionClause (fun (projection))


    def apply[SelectA : Manifest, SelectB : Manifest, SelectC : Manifest, SelectD : Manifest, SelectE : Manifest,
    Range: Manifest] (
        projection: (Rep[SelectA], Rep[SelectB], Rep[SelectC], Rep[SelectD], Rep[SelectE]) => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE[(SelectA, SelectB, SelectC, SelectD, SelectE), Range] =
        SelectProjectionClause (fun (projection))

    def apply (x: STAR_KEYWORD): SELECT_CLAUSE_STAR =
        SelectClauseStar (asDistinct = false)

    def DISTINCT[Select : Manifest, Range : Manifest] (
        projection: Rep[Select] => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE[Select, Range] =
        SelectProjectionClause (projection, asDistinct = true)


    def DISTINCT[SelectA : Manifest, SelectB : Manifest, Range : Manifest] (
        projection: (Rep[SelectA], Rep[SelectB]) => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE[(SelectA, SelectB), Range] =
        SelectProjectionClause (projection, asDistinct = true)


    def DISTINCT[SelectA : Manifest, SelectB : Manifest, SelectC : Manifest, Range : Manifest] (
        projection: (Rep[SelectA], Rep[SelectB], Rep[SelectC]) => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE [(SelectA, SelectB, SelectC), Range] =
        SelectProjectionClause (projection, asDistinct = true)


    def DISTINCT[SelectA : Manifest, SelectB : Manifest, SelectC : Manifest, SelectD : Manifest, Range : Manifest] (
        projection: (Rep[SelectA], Rep[SelectB], Rep[SelectC], Rep[SelectD]) => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE[(SelectA, SelectB, SelectC, SelectD), Range] =
        SelectProjectionClause (projection, asDistinct = true)


    def DISTINCT[SelectA : Manifest, SelectB : Manifest, SelectC : Manifest, SelectD : Manifest, SelectE : Manifest,
    Range: Manifest] (
        projection: (Rep[SelectA], Rep[SelectB], Rep[SelectC], Rep[SelectD], Rep[SelectE]) => Rep[Range]
    ): SELECT_PROJECTION_CLAUSE[(SelectA, SelectB, SelectC, SelectD, SelectE), Range] =
        SelectProjectionClause (projection, asDistinct = true)


    def DISTINCT (x: STAR_KEYWORD): SELECT_CLAUSE_STAR =
        SelectClauseStar (asDistinct = true)


    def apply[Domain: Manifest, Range: Manifest] (
        aggregation: AGGREGATE_FUNCTION[Domain, Range]
    ): SELECT_AGGREGATE_CLAUSE[Any, Domain, Range] =
        SelectAggregateClause (aggregation, asDistinct = false)


    def apply[Select : Manifest, Domain : Manifest, RangeA : Manifest, RangeB : Manifest] (
        project : Rep[Select] => Rep[RangeA],
        aggregation : AGGREGATE_FUNCTION[Domain, RangeB]
    ): SELECT_TUPLED_AGGREGATE_CLAUSE_1[Select, Domain, RangeA, RangeB] =
        	SelectTupledAggregateClause1 (project, aggregation, asDistinct = false)


    def apply[DomainA: Manifest, DomainB: Manifest, Range: Manifest] (
        aggregation: AGGREGATE_FUNCTION[(DomainA, DomainB), Range]
    ): SELECT_AGGREGATE_CLAUSE_2[Any, DomainA, DomainB, Range] =
        SelectAggregateClause2 (aggregation, asDistinct = false)


	def apply[Select : Manifest, DomainA : Manifest, DomainB : Manifest, RangeA : Manifest, RangeB : Manifest] (
		project : Rep[Select] => Rep[RangeA],
		aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB), RangeB]
	): SELECT_TUPLED_AGGREGATE_CLAUSE_2[Select, DomainA, DomainB, RangeA, RangeB] =
		SelectTupledAggregateClause2 (project, aggregation, asDistinct = false)

	def apply[DomainA: Manifest, DomainB: Manifest, DomainC : Manifest, Range: Manifest] (
		aggregation: AGGREGATE_FUNCTION[(DomainA, DomainB, DomainC), Range]
	): SELECT_AGGREGATE_CLAUSE_3[Any, DomainA, DomainB, DomainC, Range] =
		SelectAggregateClause3 (aggregation, asDistinct = false)


	def apply[Select : Manifest, DomainA : Manifest, DomainB : Manifest, DomainC : Manifest, RangeA : Manifest, RangeB : Manifest] (
		project : Rep[Select] => Rep[RangeA],
		aggregation : AGGREGATE_FUNCTION[(DomainA, DomainB, DomainC), RangeB]
  	): SELECT_TUPLED_AGGREGATE_CLAUSE_3[Select, DomainA, DomainB, DomainC, RangeA, RangeB] =
		SelectTupledAggregateClause3 (project, aggregation, asDistinct = false)

	/*

    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, Range: Manifest] (
        aggregation: AGGREGATE_FUNCTION_4[DomainA, DomainB, DomainC, DomainD, Range]
    ): SELECT_CLAUSE_4[DomainA, DomainB, DomainC, DomainD, Range] =
        throw new UnsupportedOperationException ()


    def apply[GroupKey: Manifest, GroupRange: Manifest, DomainA: Manifest, DomainB: Manifest, DomainC: Manifest,
    DomainD: Manifest, Range: Manifest] (
        project: Rep[GroupKey] => Rep[GroupRange],
        aggregation: AGGREGATE_FUNCTION_4[DomainA, DomainB, DomainC, DomainD, Range]
    ): SELECT_CLAUSE_4[DomainA, DomainB, DomainC, DomainD, (GroupRange, Range)] =
        throw new UnsupportedOperationException ()


    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, DomainE: Manifest,
    Range: Manifest] (
        aggregation: AGGREGATE_FUNCTION_5[DomainA, DomainB, DomainC, DomainD, DomainE, Range]
    ): SELECT_CLAUSE_5[DomainA, DomainB, DomainC, DomainD, DomainE, Range] =
        throw new UnsupportedOperationException ()


    def apply[GroupKey: Manifest, GroupRange: Manifest, DomainA: Manifest, DomainB: Manifest, DomainC: Manifest,
    DomainD: Manifest, DomainE: Manifest,
    Range: Manifest] (
        project: Rep[GroupKey] => Rep[GroupRange],
        aggregation: AGGREGATE_FUNCTION_5[DomainA, DomainB, DomainC, DomainD, DomainE, Range]
    ): SELECT_CLAUSE_5[DomainA, DomainB, DomainC, DomainD, DomainE, (GroupRange, Range)] =
        throw new UnsupportedOperationException ()   */


    def apply[Range: Manifest] (
        function: AGGREGATE_FUNCTION_STAR[Range]
    ): SELECT_AGGREGATE_CLAUSE_STAR[Range] =
        SelectAggregateClauseStar (function)


   def apply[Select : Manifest, RangeA : Manifest, RangeB: Manifest] (
        project : Rep[Select] => Rep[RangeA],
        function: AGGREGATE_FUNCTION_STAR[RangeB]
    ): AGGREGATE_GROUPED_SELECT_CLAUSE_STAR[Select, (RangeA, RangeB)] =
        SelectTupledAggregateClauseStar (project, function, asDistinct = false)

}
