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
package idb.algebra.opt

import org.junit.Test
import org.junit.Assert._
import scala.virtualization.lms.common.{LiftAll, ScalaOpsPkgExp}
import idb.lms.extensions.ScalaOpsExpOptExtensions
import idb.algebra.TestUtils

/**
 *
 * @author Ralf Mitschke
 *
 */
class TestIROptFusion
    extends LiftAll
    with ScalaOpsExpOptExtensions
    with ScalaOpsPkgExp
    with RelationalAlgebraIROptFusion
    with TestUtils
{

    // we require some of our own optimizations (e.g., alpha equivalence) to make the tests work
    assert (this.isInstanceOf[ScalaOpsExpOptExtensions])

    @Test
    def testSelectionFusion () {

        val f1 = (x: Rep[Int]) => x > 0
        val f2 = (x: Rep[Int]) => x < 1000
        val expA = selection (selection (emptyRelation[Int](), f1), f2)

        val f3 = (x: Rep[Int]) => (x > 0) && (x < 1000)

        val expB = selection (emptyRelation[Int](), f3)

        assertEquals (expB, expA)
    }


    @Test
    def testSelectionFusionTypingWithDifference () {
        trait A

        def infix_isA (x: Rep[A]): Rep[Boolean] = true

        trait B

        def infix_isB (x: Rep[B]): Rep[Boolean] = true

        class Impl extends A with B

        val base = emptyRelation[Impl]()

        val f1 = (x: Rep[A]) => x.isA

        val f2 = (x: Rep[B]) => x.isB

        val expA = selection (selection (base, f1), f2)

        val f3 = (x: Rep[Impl]) => x.isA && x.isB

        val expB = selection (base, f3)
        assertEquals (expB, expA)
    }

    @Test
    def testSelectionFusionTypingWithSame () {
        trait A

        def infix_isA1 (x: Rep[A]): Rep[Boolean] = true

        def infix_isA2 (x: Rep[A]): Rep[Boolean] = true

        class Impl extends A

        val base = emptyRelation[Impl]()

        val f1 = (x: Rep[A]) => x.isA1

        val f2 = (x: Rep[A]) => x.isA2

        val expA = selection (selection (base, f1), f2)

        val f3 = (x: Rep[A]) => x.isA1 && x.isA2

        val expB = selection (base, f3)
        assertEquals (expB, expA)
    }


    @Test
    def testSelectionFusionTypingWithSameMostSpecific () {
        trait A

        def infix_isA1 (x: Rep[A]): Rep[Boolean] = true

        def infix_isA2 (x: Rep[A]): Rep[Boolean] = true

        class Impl extends A

        val base = emptyRelation[Impl]()

        val f1 = (x: Rep[A]) => x.isA1

        val f2 = (x: Rep[A]) => x.isA2

        val expA = selection (selection (base, f1), f2)

        val f3 = (x: Rep[Impl]) => x.isA1 && x.isA2

        val expB = selection (base, f3)
        assertEquals (expB, expA)
    }

    @Test
    def testProjectionFusion () {

        val f1 = (x: Rep[Int]) => x + 1
        val f2 = (x: Rep[Int]) => x + 2
        val expA = projection (projection (emptyRelation[Int](), f1), f2)

        val f3 = (x: Rep[Int]) => x + 3

        val expB = projection (emptyRelation[Int](), f3)

        assertEquals (expB, expA)
    }
}
