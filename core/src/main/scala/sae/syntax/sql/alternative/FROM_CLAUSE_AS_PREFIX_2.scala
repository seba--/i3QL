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
package sae.syntax.sql.alternative

import sae.syntax.sql.{STAR_KEYWORD, FROM_CLAUSE_2}

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 21:11
 *
 */
trait FROM_CLAUSE_AS_PREFIX_2[DomainA <: AnyRef, DomainB <: AnyRef]
{

    def SELECT[Range <: AnyRef](projection: (DomainA, DomainB) => Range): FROM_CLAUSE_2[DomainA, DomainB, Range]

    def SELECT[RangeA <: AnyRef, RangeB](projectionA: DomainA => RangeA, projectionB: DomainB => RangeB): FROM_CLAUSE_2[DomainA, DomainB, (RangeA, RangeB)]

    def SELECT(x: STAR_KEYWORD): FROM_CLAUSE_2[DomainA, DomainB, (DomainA, DomainB)]

    def SELECT[Range <: AnyRef](distinct: DISTINCT_INFIX_SELECT_CLAUSE[(DomainA, DomainB), Range]): FROM_CLAUSE_2[DomainA, DomainB, Range]

    def SELECT(distinct: DISTINCT_INFIX_SELECT_CLAUSE_NO_PROJECTION): FROM_CLAUSE_2[DomainA, DomainB, (DomainA, DomainB)]

}