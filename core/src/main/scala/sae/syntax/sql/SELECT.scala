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

import impl.{Projection2, NoProjection, Projection}

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 19:53
 *
 */
object SELECT
    extends STARTING_CLAUSE_PREFIX_SELECT
{

    def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range): SELECT_CLAUSE[Domain, Range] =
        Projection (projection, distinct = false)

    def apply[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](projection: (DomainA, DomainB) => Range): SELECT_CLAUSE_2[DomainA, DomainB, Range] =
        Projection2 (projection, distinct = false)

    def apply(x: STAR_KEYWORD): SELECT_CLAUSE_NO_PROJECTION = NoProjection (distinct = false)

    def DISTINCT[Domain <: AnyRef, Range <: AnyRef](projection: (Domain) => Range): SELECT_CLAUSE[Domain, Range] =
        Projection (projection, distinct = true)

    def DISTINCT(x: STAR_KEYWORD): SELECT_CLAUSE_NO_PROJECTION =
        NoProjection (distinct = true)
}
