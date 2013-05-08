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


import idb.syntax.iql.impl.{SelectClauseNoProjection, SelectClause1, SelectClause2}

/**
 *
 * @author Ralf Mitschke
 */
object SELECT
{

    def apply[Domain, Range] (projection: Exp[Domain => Range]): SELECT_CLAUSE_1[Domain, Range] =
        SelectClause1 (projection)

    def apply[DomainA, DomainB, Range] (projection: Exp[(DomainA, DomainB) => Range]
                                       ): SELECT_CLAUSE_2[DomainA, DomainB, Range] =
        SelectClause2 (projection)

    def apply[DomainA, DomainB, RangeA, RangeB] (projectionA: Exp[DomainA => RangeA],
                                                 projectionB: Exp[DomainB => RangeB]
                                                ): SELECT_CLAUSE_2[DomainA, DomainB, (RangeA, RangeB)] =
        throw new UnsupportedOperationException()
        //SelectClause2 (fun ((a: Exp[DomainA], b: Exp[DomainB]) => (projectionA (a), projectionB (b))))

    def apply (x: STAR_KEYWORD): SELECT_CLAUSE_NO_PROJECTION =
        SelectClauseNoProjection

}
