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
import idb.syntax.iql.impl.AggregateFunctionSelfMaintained

/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 *
 */

trait AGGREGATE_FUNCTION_FACTORY_SELF_MAINTAINED[Column, Range]
	extends AGGREGATE_FUNCTION_FACTORY[Column, Range]
{
	def start : Range

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
	): AGGREGATE_FUNCTION[Domain, Range] =
        AggregateFunctionSelfMaintained[Domain, Range](
			start,
			(p : Rep[(Domain, Range)]) => added(p._1,p._2, column),
			(p : Rep[(Domain, Range)]) => removed(p._1,p._2, column),
			(p : Rep[(Domain, Domain, Range)]) => updated(p._1, p._2, p._3, column)
		)

    def apply[DomainA, DomainB] (
        column: (Rep[DomainA], Rep[DomainB]) => Rep[Column]
    )(
		implicit mDomA : Manifest[DomainA], mDomB : Manifest[DomainB], mRan : Manifest[Range]
	) = {
		val c = (v : Rep[(DomainA, DomainB)]) => column(v._1, v._2)

		AggregateFunctionSelfMaintained[(DomainA, DomainB), Range](
			start,
			(p : Rep[((DomainA, DomainB), Range)]) => added(p._1, p._2, c),
			(p : Rep[((DomainA, DomainB), Range)]) => removed(p._1, p._2, c),
			(p : Rep[((DomainA, DomainB), (DomainA, DomainB), Range)]) => updated(p._1, p._2, p._3, c)
		)
	}
}
