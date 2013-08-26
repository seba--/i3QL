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
import idb.syntax.iql.impl.AggregateFunction1

/**
 *
 * @author Ralf Mitschke
 *
 */

trait AGGREGATE_FUNCTION_FACTORY[Column, Range]
{
	def start : Rep[Range]

	def added[Domain] (v: Rep[Domain],
		previousResult: Rep[Range],
		column: Rep[Domain] => Rep[Column]
	) : Rep[Range]

	def removed[Domain] (v: Rep[Domain],
		previousResult: Rep[Range],
		column: Rep[Domain] => Rep[Column]
	) : Rep[Range]

	def updated[Domain] (oldV: Rep[Domain],
		newV : Rep[Domain],
		previousResult: Rep[Range],
		column: Rep[Domain] => Rep[Column]
	) : Rep[Range]

    def apply[Domain] (
        column: Rep[Domain] => Rep[Column]
    )(
		implicit mDom : Manifest[Domain], mRan : Manifest[Range]
	): AGGREGATE_FUNCTION_1[Domain, Range] =
        AggregateFunction1[Domain, Range](
			start,
			(p : Rep[(Domain, Range)]) => added(p._1,p._2,column),
			(p : Rep[(Domain, Range)]) => removed(p._1,p._2,column),
			(p : Rep[(Domain, Domain, Range)]) => updated(p._1, p._2, p._3, column)
		)



    def apply[DomainA: Manifest, DomainB: Manifest] (
        column: (Rep[DomainA], Rep[DomainB]) => Rep[Column]
    ): AGGREGATE_FUNCTION_2[DomainA, DomainB, Range] =
        null


    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest] (
        column: (Rep[DomainA], Rep[DomainB], Rep[DomainC]) => Rep[Column]
    ): AGGREGATE_FUNCTION_3[DomainA, DomainB, DomainC, Range] =
        null


    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest] (
        column: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD]) => Rep[Column]
    ): AGGREGATE_FUNCTION_4[DomainA, DomainB, DomainC, DomainD, Range] =
        null


    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, DomainE: Manifest] (
        column: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD], Rep[DomainE]) => Rep[Column]
    ): AGGREGATE_FUNCTION_5[DomainA, DomainB, DomainC, DomainD, DomainE, Range] =
        null


    def apply (star: STAR_KEYWORD): AGGREGATE_FUNCTION_STAR[Range] = null
}
