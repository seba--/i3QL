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


import idb.query.colors._

import scala.virtualization.lms.common._
import org.junit.Test
import org.junit.Ignore
import org.junit.Assert._

/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 */
class TestFunctionUtils
    extends BaseFatExp
    with NumericOpsExp
    with EffectExp
    with EqualExp
    with TupledFunctionsExp
    with TupleOpsExp
    with BooleanOpsExp
    with LiftAll
    with FunctionUtils
	with PrimitiveOpsExp
{

    @Test
    def testFunction1Parameter () {

        val f = (i: Rep[Int]) => {1 + i }

        assertEquals (
            1,
            parameters (f).size
        )

        assertEquals (
            manifest[Int],
            parameters (f)(0).tp
        )
    }

    @Test
    def testFunction2ParameterBoxed () {

        val f = (i: Rep[Int], j: Rep[Boolean]) => {(i == 0) && j }

        assertEquals (
            2,
            parameters (f).size
        )

        assertEquals (
            manifest[Int],
            parameters (f)(0).tp
        )

        assertEquals (
            manifest[Boolean],
            parameters (f)(1).tp
        )
    }

    @Test
    def testFunction2ParameterTupled () {

        val f = (t: Rep[(Int, Boolean)]) => {(t._1 == 0) && t._2 }

        assertEquals (
            2,
            parameters (f).size
        )

        assertEquals (
            manifest[Int],
            parameters (f)(0).tp
        )

        assertEquals (
            manifest[Boolean],
            parameters (f)(1).tp
        )
    }


    @Test
    def testFunction1FreeVar () {

        val f = (i: Rep[Int]) => {1 + 5 }

        assertEquals (
            1,
            freeVars (f).size
        )

        assertEquals (
            manifest[Int],
            freeVars (f)(0).tp
        )
    }

    @Test
    def testFunction1UsedVar () {

        val f = (i: Rep[Int]) => {i + 1 }

        assertEquals (
            0,
            freeVars (f).size
        )

    }

    @Test
    def testFunction2FreeVar1st () {

        val f = (i: Rep[Int], j: Rep[Boolean]) => {!j }

        assertEquals (
            1,
            freeVars (f).size
        )

        assertEquals (
            manifest[Int],
            freeVars (f)(0).tp
        )
    }

    @Test
    def testFunction2FreeVar2nd () {

        val f = (i: Rep[Int], j: Rep[Boolean]) => {i + 1 }

        assertEquals (
            1,
            freeVars (f).size
        )

        assertEquals (
            manifest[Boolean],
            freeVars (f)(0).tp
        )
    }

    @Test
    def testFunction2FreeVar1stAnd2nd () {
        val g = () => {unit (false) }
        val f = (i: Rep[Int], j: Rep[Boolean]) => {g () || 32 < 40 }

        assertEquals (
            2,
            freeVars (f).size
        )
    }


    @Test
    def testFunction1UsedVar1stAnd2nd () {

        val f = (i: Rep[Int], j: Rep[Boolean]) => {(i + 1) == 0 && j }

        assertEquals (
            0,
            freeVars (f).size
        )

    }

	@Test
	def testReturnedParameterIndex1(): Unit = {
		val f = (i : Rep[Int], j : Rep[Int]) => i

		val func = (function: Rep[_ => _]) => function

		func(f) match {
			case Def(a) => Predef.println(a)
			case _ =>
		}

		assertEquals(
			0,
			returnedParameter (f)
		)
	}

	@Test
	def testReturnedParameterIndex2(): Unit = {
		val f = (i : Rep[Int], j : Rep[Int]) => j

		assertEquals(
			1,
			returnedParameter (f)
		)
	}

	@Test
	def testReturnedParameterIndex3(): Unit = {
		val f = (i : Rep[Int], j : Rep[Int]) => i + j

		assertEquals(
			-1,
			returnedParameter (f)
		)
	}

	@Test
	def testReturnedParameterIndex4(): Unit = {
		val f = (i : Rep[Int], j : Rep[Int], k : Rep[Int], l : Rep[Int]) => k

		assertEquals(
			2,
			returnedParameter (f)
		)
	}

	@Test
	def testReturnedParameterIndex5(): Unit = {
		val f = (t : Rep[(Int, Int)]) => t._2

		assertEquals(
			1,
			returnedParameter (f)
		)
	}

	@Test
	def testIsIdentity1(): Unit = {
		val f = (i : Rep[Int]) => i

		assertTrue(
			isIdentity(f)
		)
	}



	@Ignore //TODO: Should this even work?
	@Test
	def testIsIdentity2(): Unit = {
		val f = (t : Rep[(Int, Int)]) => (t._1, t._2)

		assertTrue(
			isIdentity(f)
		)
	}

	@Test
	def testIsIdentity3(): Unit = {
		val f = (t : Rep[(Int, Int)]) => (t._2, t._1)

		assertFalse(
			isIdentity(f)
		)
	}

	@Test
	def testIsIdentity4(): Unit = {
		val f = (i : Rep[Int]) => 0

		assertFalse(
			isIdentity(f)
		)
	}



	@Test
	def testReturnsFromTuple2_1(): Unit = {
		val f = (t : Rep[(Int, Int)]) => t._1

		assertTrue (
			returnsLeftOfTuple2(f)
		)

		assertFalse (
			returnsRightOfTuple2(f)
		)
	}

	@Test
	def testReturnsFromTuple2_2(): Unit = {
		val f = (t : Rep[(Int, Int)]) => t._2

		assertFalse (
			returnsLeftOfTuple2(f)
		)

		assertTrue (
			returnsRightOfTuple2(f)
		)
	}

	@Test
	def testReturnsFromTuple2_3(): Unit = {
		val f = (t : Rep[(Int, Int)]) => t

		assertFalse (
			returnsLeftOfTuple2(f)
		)

		assertFalse (
			returnsRightOfTuple2(f)
		)
	}




	@Test
	def testDisjunctiveParameterEquality1(): Unit = {
		val f = (i : Rep[Int], j : Rep[Int]) => i == j

		assertTrue(
			isDisjunctiveParameterEquality(f)
		)
	}

	@Test
	def testDisjunctiveParameterEquality2(): Unit = {
		val f = (i : Rep[Int], j : Rep[Int]) => i + 1 == j

		assertTrue(
			isDisjunctiveParameterEquality(f)
		)
	}

	@Test
	def testDisjunctiveParameterEquality3(): Unit = {
		val f = (i : Rep[Int], j : Rep[Int]) => i + j == 0

		assertFalse(
			isDisjunctiveParameterEquality(f)
		)
	}
}
