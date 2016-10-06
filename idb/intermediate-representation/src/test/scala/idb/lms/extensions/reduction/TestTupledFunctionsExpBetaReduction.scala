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

import org.junit.Test
import idb.lms.extensions.equivalence._
import idb.lms.extensions.LMSTestUtils

/**
 *
 * @author Ralf Mitschke
 *
 */
class TestTupledFunctionsExpBetaReduction
    extends TupledFunctionsExpBetaReduction
    with TupledFunctionsExpAlphaEquivalence
    with StaticDataExpAlphaEquivalence
    with StructExpAlphaEquivalence
    with ScalaOpsPkgExpAlphaEquivalence
    with LMSTestUtils
{


    @Test
    def testApply1ToSymParameter () {
        val f1 = fun ((i: Rep[Int]) => i + 1)

        val f2 = fun ((i: Rep[Int]) => f1 (i))

        assertEqualFunctions (f2, f1)
    }

    @Test
    def testApply1ToUnboxedParameter1 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => i + 1)

        val f2 = fun ((i: Rep[(Int, Int)]) => f1 (i))

        assertEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)]) => i._1 + 1)

        assertEqualFunctions (f3, f2)
    }

    @Test
    def testApplyTupleToUnboxedParameter1 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => i)

        val f2 = fun ((i: Rep[Int], j: Rep[Int]) => f1 (i, j))

        assertEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)]) => i._1)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApply1ToUnboxedParameter2 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => j + 1)

        val f2 = fun ((i: Rep[(Int, Int)]) => f1 (i))

        assertEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)]) => i._2 + 1)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApplyTupleToUnboxedParameter2 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => j)

        val f2 = fun ((i: Rep[Int], j: Rep[Int]) => f1 (i, j))

        assertEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)]) => i._2)

        assertEqualFunctions (f3, f2)
    }

    @Test
    def testApply1ToIdentityUnboxedParameter1 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => i)

        val f2 = fun ((i: Rep[(Int, Int)]) => f1 (i))

        assertEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)]) => i._1)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApply1ToIdentityUnboxedParameter2 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => j)

        val f2 = fun ((i: Rep[(Int, Int)]) => f1 (i))

        assertEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)]) => i._2)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApply1ToTupleParameter1 () {
        val f1 = fun ((i: Rep[(Int, Int)]) => i._1 + 1)

        val f2 = fun ((i: Rep[(Int, Int)]) => f1 (i))

        assertEqualFunctions (f2, f1)

    }


    @Test
    def testApply1ToTupleParameter2 () {
        val f1 = fun ((i: Rep[(Int, Int)]) => i._2 + 1)

        val f2 = fun ((i: Rep[(Int, Int)]) => f1 (i))

        assertEqualFunctions (f2, f1)

    }


    @Test
    def testApply1ToTupleParameterBoth () {
        val f1 = fun ((i: Rep[(Int, Int)]) => i._2 + i._1)

        val f2 = fun ((i: Rep[(Int, Int)]) => f1 (i))

        assertEqualFunctions (f2, f1)

    }


    @Test
    def testApply2ToUnboxedParameter1 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => i + 1)

        val f2 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => f1 (i) + j)

        assertNotEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => i._1 + 1 + j)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApply2ToUnboxedParameter2 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => j + 1)

        val f2 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => f1 (i) + j)

        assertNotEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => i._2 + 1 + j)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApply2ToIdentityUnboxedParameter1 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => i)

        val f2 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => f1 (i) + j)

        assertNotEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => i._1 + j)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApply2ToIdentityUnboxedParameter2 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => j)

        val f2 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => f1 (i) + j)

        assertNotEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => i._2 + j)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApply2ToTupleParameter1 () {
        val f1 = fun ((i: Rep[(Int, Int)]) => i._1 + 1)

        val f2 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => f1 (i) + j)

        assertNotEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => i._1 + 1 + j)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApply2ToTupleParameter2 () {
        val f1 = fun ((i: Rep[(Int, Int)]) => i._2 + 1)

        val f2 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => f1 (i) + j)

        assertNotEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => i._2 + 1 + j)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApply2ToTupleParameterBoth () {
        val f1 = fun ((i: Rep[(Int, Int)]) => i._2 + i._1)

        val f2 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => f1 (i) + j)

        assertNotEqualFunctions (f2, f1)

        val f3 = fun ((i: Rep[(Int, Int)], j: Rep[Int]) => i._2 + i._1 + j)

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApplyMakeTupleOnIdentityUnboxedParameter1 () {
        val f1 = fun ((i: Rep[Int], j: Rep[Int]) => i)

        val f2 = fun ((x: Rep[Int], y: Rep[(Int, Int)]) => (x, f1 (y)))

        assertNotEqualFunctions (f2, f1)

        val f3 = fun ((x: Rep[Int], y: Rep[(Int, Int)]) => (x, y._1))

        assertEqualFunctions (f3, f2)
    }


    @Test
    def testApplOnStaticFunctionWithEquivalence () {
        val fStatic = staticData ((x: (Int, Int)) => x._1 + x._2)

        val f2 = fun ((x: Rep[Int], y: Rep[Int]) => fStatic (x, y))

        val f3 = fun ((x: Rep[(Int, Int)]) => fStatic (x._1, x._2))

        assertNotEqualFunctions (f2, fStatic)
        assertNotEqualFunctions (f3, fStatic)

        assertEqualFunctions (f3, f2)
    }

}
