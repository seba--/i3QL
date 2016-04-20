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

import idb.query.QueryEnvironment
import org.junit.{Ignore, Test}
import org.junit.Assert._
import scala.virtualization.lms.common.LiftAll
import idb.algebra.TestUtils
import idb.algebra.fusion.RelationalAlgebraIRFuseBasicOperators
import idb.lms.extensions.ScalaOpsExpOptExtensions
import idb.algebra.print.RelationalAlgebraPrintPlanBasicOperators

/**
 *
 * @author Ralf Mitschke
 *
 */
class TestIROptFusion
    extends RelationalAlgebraIRFuseBasicOperators
    with RelationalAlgebraPrintPlanBasicOperators
    with ScalaOpsExpOptExtensions
    with LiftAll
    with TestUtils
{

    // needs binding for printing relation
    override val IR: this.type = this

    override def reset { super.reset }

	override def isPrimitiveType[T](m: Manifest[T]) : Boolean =
		super.isPrimitiveType[T](m)

    @Test
    def testSelectionFusion () {
		implicit val local = QueryEnvironment.Local

		val f1 = fun ((x: Rep[Int]) => x > 0)
        val f2 = fun ((x: Rep[Int]) => x < 1000)
        val expA = selection (selection (emptyRelation[Int], f1), f2)

        val f3 = fun((x: Rep[Int]) => f1 (x) && f2 (x)) //(x > 0) && (x < 1000)

        val expB = selection (emptyRelation[Int], f3)

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }


    @Test
    def testSelectionFusionTypingWithDifference () {
		implicit val local = QueryEnvironment.Local

		trait A

        def infix_isA (x: Rep[A]): Rep[Boolean] = true

        trait B

        def infix_isB (x: Rep[B]): Rep[Boolean] = true

        class Impl extends A with B

        val base = emptyRelation[Impl]

        val f1 = fun ((x: Rep[A]) => x.isA)

        val f2 = fun ((x: Rep[B]) => x.isB)

        val expA = selection (selection (base, f1), f2)

        val f3 = (x: Rep[Impl]) => f1 (x) && f2 (x) // x.isA && x.isB

        val expB = selection (base, f3)
        assertEquals (expB, expA)
    }

    @Ignore
    @Test
    def testSelectionFusionTypingWithSame () {
		implicit val local = QueryEnvironment.Local

		trait A

        def infix_isA1 (x: Rep[A]): Rep[Boolean] = true

        def infix_isA2 (x: Rep[A]): Rep[Boolean] = true

        class Impl extends A

        val base = emptyRelation[Impl]

        val f1 = fun ((x: Rep[A]) => x.isA1)

        val f2 = fun ((x: Rep[A]) => x.isA2)


        val expA = selection (selection (base, f1), f2)

        // TODO this should work with Rep[A],
        // but the inner function composition of the selects will take the domain of relation (i.e., base)
        val f3 = (x: Rep[A]) => f1 (x) && f2 (x) // x.isA1 && x.isA2

        val expB = selection (base, f3)
        assertEquals (expB, expA)
    }


    @Test
    def testSelectionFusionTypingWithSameMostSpecific () {
		implicit val local = QueryEnvironment.Local

		trait A

        def infix_isA1 (x: Rep[A]): Rep[Boolean] = true

        def infix_isA2 (x: Rep[A]): Rep[Boolean] = true

        class Impl extends A

        val base = emptyRelation[Impl]

        val f1 = fun ((x: Rep[A]) => x.isA1)

        val f2 = fun ((x: Rep[A]) => x.isA2)

        val expA = selection (selection (base, f1), f2)

        val f3 = (x: Rep[Impl]) => f1 (x) && f2 (x) // x.isA1 && x.isA2

        val expB = selection (base, f3)
        assertEquals (expB, expA)
    }

    @Test
    def testProjectionFusion () {
		implicit val local = QueryEnvironment.Local

		val f1 = fun ((x: Rep[Int]) => x + 1)
        val f2 = fun ((x: Rep[Int]) => x + 2)
        val expA = projection (projection (emptyRelation[Int], f1), f2)

        val f3 = (x: Rep[Int]) => f2 (f1 (x)) // x + 3

        val expB = projection (emptyRelation[Int], f3)

        assertEquals (expB, expA)
    }

    @Test
    def testSelectionFusionWithStaticDataFunction() {
		implicit val local = QueryEnvironment.Local

		val f1 = staticData( (x:Int) => x != 0)

        val f2 = fun((x:Rep[Int]) => x == 100)

        val expA = selection (selection (emptyRelation[Int], f1), f2)

        val f3 = fun((x: Rep[Int]) => f1 (x) && f2 (x))

        val expB = selection (emptyRelation[Int], f3)

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }

    @Test
    def testSelectionFusionWithIndirectStaticDataFunction() {
		implicit val local = QueryEnvironment.Local

		val f1 = staticData( (x:Int) => x != 0)

        val f2 = fun((x:Rep[Int]) => x == 100)

        val f1Ind = fun((x:Rep[Int]) => f1(x))

        val expA = selection (selection (emptyRelation[Int], f1Ind), f2)

        val f3 = fun((x: Rep[Int]) => f1Ind (x) && f2 (x))

        val expB = selection (emptyRelation[Int], f3)

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }



    @Test
    def testSelectionFusionWithStaticDataAndDynamicFunction() {
		implicit val local = QueryEnvironment.Local

		val f1 = staticData( (x:Int) => x != 0)
        val f2 = fun((x:Rep[Int]) => x == 100)
        val f1Ind = fun((x:Rep[Int]) => f1(x))

        val dynF1 = dynamicLambda(parameter(f1Ind), body(f1Ind))
        val dynF2 = dynamicLambda(parameter(f2), body(f2))

        val expA = selection (selection (emptyRelation[Int], dynF1), dynF2)

        val f3 = fun((x: Rep[Int]) => dynF1 (x) && dynF2 (x))

        val expB = selection (emptyRelation[Int], f3)

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }


    @Test
    def testSelectionFusionWithStaticDataAndDynamicFunctions2() {
		implicit val local = QueryEnvironment.Local

		val fStatic = staticData( (x:String) => x.hashCode)

        val f1 = fun((x:Rep[Int]) => x > fStatic("hello"))
        val f2 = fun((x:Rep[Int]) => x > fStatic("world"))

        val dynF1 = dynamicLambda(parameter(f1), body(f1))
        val dynF2 = dynamicLambda(parameter(f2), body(f2))

        val expA = selection (selection (emptyRelation[Int], dynF1), dynF2)

        val f3 = fun((x: Rep[Int]) =>  x > fStatic("hello") && x > fStatic("world"))

        val expB = selection (emptyRelation[Int], f3)

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }

}
