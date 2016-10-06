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
package idb.lms.extensions

import org.junit.Assert._
import org.junit.{Assert, Ignore, Test}
import org.hamcrest.CoreMatchers._
import scala.virtualization.lms.common._

/**
 *
 * @author Mirko Köhler
 */
class TestExpressionUtils
    extends BaseFatExp
    with NumericOpsExp
    with EffectExp
    with EqualExp
    with TupledFunctionsExp
    with TupleOpsExp
    with BooleanOpsExp
    with LiftAll
    with ExpressionUtils
	with PrimitiveOpsExp
{

    @Test
    def testFindSyms1 () {

        val func : Rep[Int => Int] = (i: Rep[Int]) => {1 + i }

		func match {
			case Def(Lambda(f, x, y)) =>
				assertThat (findSyms(y.res)(Set(x)), is (Set(x)))
			case _ => Assert.fail()
		}
    }

	@Test
	def testFindSyms2 () {

		val func : Rep[((Int, Int)) => Int] = (i: Rep[Int], j: Rep[Int]) => j + i

		func match {
			case Def(Lambda(f, UnboxedTuple(xs), y)) =>
				assertThat (findSyms(y.res)(xs.toSet), is (xs.toSet))
			case _ => Assert.fail()
		}
	}

	@Test
	def testFindSyms3 () {

		val func : Rep[((Int, Int)) => Int] = (i: Rep[Int], j: Rep[Int]) => j + 1

		func match {
			case Def(Lambda(f, UnboxedTuple(xs), y)) =>
				assertThat (findSyms(y.res)(xs.toSet), is (Set(xs(1))))
			case _ => Assert.fail()
		}
	}

	@Test
	def testFindSyms4 () {

		val func : Rep[((Int, Int)) => Int] = (t: Rep[(Int, Int)]) => t._2

		func match {
			case Def(Lambda(f, x@UnboxedTuple(_), y)) =>
				assertThat(findSyms(y.res)(Set(field(x, "_1"))).size, is (0))
				assertThat(findSyms(y.res)(Set(field(x, "_2"))).size, is (1))
			case _ => Assert.fail()
		}
	}




}
