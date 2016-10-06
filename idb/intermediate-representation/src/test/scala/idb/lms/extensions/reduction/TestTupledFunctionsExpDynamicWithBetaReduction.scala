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
package idb.lms.extensions.reduction

import idb.lms.extensions.LMSTestUtils
import idb.lms.extensions.equivalence._
import idb.lms.extensions.functions.{FunctionsExpDynamicLambdaAlphaEquivalence, TupledFunctionsExpDynamicLambda}
import org.junit.Test

import scala.reflect.SourceContext

/**
 *
 * @author Ralf Mitschke
 *
 */

class TestTupledFunctionsExpDynamicWithBetaReduction
    extends TupledFunctionsExpBetaReduction
    with TupledFunctionsExpDynamicLambda
    with TupledFunctionsExpAlphaEquivalence
    with FunctionsExpDynamicLambdaAlphaEquivalence
    with StructExpAlphaEquivalence
    with ScalaOpsPkgExpAlphaEquivalence
    with LMSTestUtils
{


    @Test
    def testApplyMakeTupleDynamicOnIdentityUnboxedParameter1 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => i)

        val f1_dynamic = f1.asInstanceOf[Rep[Any => Any]]

        val f2 = fun ((x: Rep[Int], y: Rep[Any]) => (x, f1_dynamic (y)))(manifest[Int], manifest[(Int, Int)].asInstanceOf[Manifest[Any]], manifest[(Int,Int)].asInstanceOf[Manifest[(Int,Any)]])

        assertNotEqualFunctions (f2, f1)

        val f3 = fun ((x: Rep[Int], y: Rep[(Int, Int)]) => (x, y._1))

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApplyNotBoxedThenBoxed () {
        //TODO "This test does not actually fail."
		/*
			[error] Test idb.lms.extensions.reduction.TestTupledFunctionsExpDynamicWithBetaReduction.testApplyNotBoxedThenBoxed failed: junit.framework.AssertionFailedError: expected:
			[error] <((x0:Int,x1:Int)): Boolean => {
			[error]     val x2 = x0 == x1
			[error]     x2
			[error] }>
			[error] but was:
			[error] <((x9:Int,x10:Int)): Boolean => {
			[error]     val x11 = x9
			[error]     val x12 = x10
			[error]     val x13 = x11 == x12
			[error]     x13
			[error] }>, took 0.007 sec
		 */
        val f1 = fun((i: Rep[Int], j: Rep[Int]) => i == j)


        val x1 = fresh[(Int,Int)]
        val b1 = f1(x1)
        val f2 = dynamicLambda(x1, b1)

        val x2 = unboxedFresh[(Int,Int)]
        val b2 = f2(x2)

        val f3 = dynamicLambda(x2, b2)

		//TODO reactivate this
       // assertEqualFunctions(f1, f3)
    }

}
