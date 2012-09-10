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
package sae.syntax.sql

import ast.{AggregateSelectClause1, AggregateSelectClause2, SelectClause2, SelectClause1}
import impl.{SelectClauseAggregationNoProjectionSyntax, SelectClause2Syntax, SelectClauseNoProjectionSyntax, SelectClause1Syntax}
import sae.functions.Count
import sae.operators.SelfMaintainableAggregateFunction

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:53
 *
 */
object SELECT
{

    def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range): SELECT_CLAUSE[Domain, Range] =
        SelectClause1Syntax (SelectClause1 (Some (projection)))

    def apply[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: (DomainA, DomainB) => Range): SELECT_CLAUSE_2[DomainA, DomainB, Range] =
        SelectClause2Syntax (SelectClause2 (Some (projection)))

    def apply[DomainA <: AnyRef, DomainB <: AnyRef, RangeA <: AnyRef, RangeB <: AnyRef](projectionA: DomainA => RangeA,
                                                                                        projectionB: DomainB => RangeB): SELECT_CLAUSE_2[DomainA, DomainB, (RangeA, RangeB)] =
        SelectClause2Syntax (
            SelectClause2 (
                Some ((a: DomainA, b: DomainB) => (projectionA (a), projectionB (b)))
            )
        )


    def apply(x: STAR_KEYWORD): SELECT_CLAUSE_NO_PROJECTION =
        SelectClauseNoProjectionSyntax ()

    def DISTINCT[Domain <: AnyRef, Range <: AnyRef](projection: (Domain) => Range): SELECT_CLAUSE[Domain, Range] =
        SelectClause1Syntax (
            SelectClause1 (
                Some (projection),
                distinct = true
            )
        )

    def DISTINCT[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: (DomainA, DomainB) => Range): SELECT_CLAUSE_2[DomainA, DomainB, Range] =
        SelectClause2Syntax (
            SelectClause2 (
                Some (projection),
                distinct = true
            )
        )

    def DISTINCT[DomainA <: AnyRef, DomainB <: AnyRef, RangeA <: AnyRef, RangeB <: AnyRef](projectionA: DomainA => RangeA,
                                                                                           projectionB: DomainB => RangeB): SELECT_CLAUSE_2[DomainA, DomainB, (RangeA, RangeB)] =
        SelectClause2Syntax (
            SelectClause2 (
                Some ((a: DomainA, b: DomainB) => (projectionA (a), projectionB (b))),
                distinct = true
            )
        )


    def DISTINCT(x: STAR_KEYWORD): SELECT_CLAUSE_NO_PROJECTION =
        SelectClauseNoProjectionSyntax (distinct = true)

    def COUNT[Domain <: AnyRef, Range <: AnyRef](projection: (Domain) => Range): SELECT_CLAUSE[Domain, Some[Int]] =
        SelectClause1Syntax (
            AggregateSelectClause1[Domain, Range, Int, Some[Int], SelfMaintainableAggregateFunction[Range, Int]](
                Some (projection),
                Count[Range](),
                distinct = false
            )
        )

    def COUNT[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: (DomainA, DomainB) => Range): SELECT_CLAUSE_2[DomainA, DomainB, Some[Int]] =
        SelectClause2Syntax (
            AggregateSelectClause2[DomainA, DomainB, Range, Int, Some[Int], SelfMaintainableAggregateFunction[Range, Int]](
                Some (projection),
                Count[Range](),
                distinct = false
            )
        )

    def COUNT[DomainA <: AnyRef, DomainB <: AnyRef, RangeA <: AnyRef, RangeB <: AnyRef](projectionA: DomainA => RangeA,
                                                                                        projectionB: DomainB => RangeB): SELECT_CLAUSE_2[DomainA, DomainB, Some[Int]] =
        SelectClause2Syntax (
            AggregateSelectClause2[DomainA, DomainB, (RangeA, RangeB), Int, Some[Int], SelfMaintainableAggregateFunction[(RangeA, RangeB), Int]](
                Some ((a: DomainA, b: DomainB) => (projectionA (a), projectionB (b))),
                Count[(RangeA, RangeB)](),
                distinct = false
            )
        )


    def COUNT(x: STAR_KEYWORD): SELECT_CLAUSE_AGGREGATION_NO_PROJECTION =
        SelectClauseAggregationNoProjectionSyntax (distinct = false)
}
