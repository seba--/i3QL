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
import idb.syntax.iql.impl.AggregateFunctionStar

/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 *
 */

trait AGGREGATE_FUNCTION_FACTORY[Column, Range]
{

    def apply[Domain] (
        column: Rep[Domain] => Rep[Column]
    )(
		implicit mDom : Manifest[Domain], mRan : Manifest[Range]
	): AGGREGATE_FUNCTION_1[Domain, Range]


    def apply[DomainA, DomainB] (
        column: (Rep[DomainA], Rep[DomainB]) => Rep[Column]
    )(
		implicit mDomA : Manifest[DomainA], mDomB : Manifest[DomainB], mRan : Manifest[Range]
	): AGGREGATE_FUNCTION_2[(DomainA, DomainB), Range]


	def apply[DomainA, DomainB, DomainC] (
		column: (Rep[DomainA], Rep[DomainB], Rep[DomainC]) => Rep[Column]
	)(
		implicit mDomA : Manifest[DomainA], mDomB : Manifest[DomainB], mDomC : Manifest[DomainC], mRan : Manifest[Range]
	): AGGREGATE_FUNCTION_3[(DomainA, DomainB, DomainC), Range]


	def apply[DomainA, DomainB, DomainC, DomainD] (
		column: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD]) => Rep[Column]
	)(
		implicit mDomA : Manifest[DomainA], mDomB : Manifest[DomainB], mDomC : Manifest[DomainC], mDomD : Manifest[DomainD], mRan : Manifest[Range]
	): AGGREGATE_FUNCTION_4[(DomainA, DomainB, DomainC, DomainD), Range]


	def apply[DomainA, DomainB, DomainC, DomainD, DomainE] (
		column: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD], Rep[DomainE]) => Rep[Column]
	)(
		implicit mDomA : Manifest[DomainA], mDomB : Manifest[DomainB], mDomC : Manifest[DomainC], mDomD : Manifest[DomainD], mDomE : Manifest[DomainE], mRan : Manifest[Range]
	): AGGREGATE_FUNCTION_5[(DomainA, DomainB, DomainC, DomainD, DomainE), Range]


    def apply (
		star: STAR_KEYWORD
	)(
		implicit mDom : Manifest[Column], mRan : Manifest[Range]
	) : AGGREGATE_FUNCTION_STAR[Range] =
        AggregateFunctionStar (this)

}
