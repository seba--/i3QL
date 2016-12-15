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

import idb.lms.extensions.operations.StringOpsExpExt
import idb.query.taint._
import org.junit.Assert._
import org.junit.{Ignore, Test}

import scala.virtualization.lms.common._

/**
 *
 * @author Mirko Köhler
 */
class TestRemoteUtils
    extends BaseFatExp
    with NumericOpsExp
    with EffectExp
    with EqualExp
    with TupledFunctionsExp
    with TupleOpsExp
    with BooleanOpsExp
    with LiftAll
    with RemoteUtils
	with PrimitiveOpsExp
	with StringOpsExpExt
{


	@Test
	def testTaintsInExp1(): Unit = {
		val variable : Exp[Int] = 0 + 1

		val exp = int_plus(
			field(
				field(variable, "_1"),
				"count"
			),
			1
		)

		val taint = RecordTaint(
			Map(
				FieldName("_1") -> RecordTaint(Map(FieldName("count") -> Taint("blue"))),
			    FieldName("_2") -> Taint("yellow")
			)
		)

		println(taintOfBaseExp(exp, variable, taint))



	}

	@Test
	def testTaintsInFun1(): Unit = {
		val f = (t : Rep[((Int, Int), Int)]) => t._1._2 + 1

		val taint = Taint(
				"_1" -> Taint("_1" -> Taint("red"), "_2" -> Taint("blue")),
				"_2" -> Taint("yellow")
		)

		assertEquals(Set(Taint("blue")), taintOfBase(f, taint))
	}

	@Test
	def testTaintsInFun2(): Unit = {
		val f = (t : Rep[((Int, Int), Int)]) => t._1._2 + t._2

		val taint = RecordTaint(
			Map(
				FieldName("_1") -> RecordTaint(Map(FieldName("_1") -> Taint("red"), FieldName("_2") -> Taint("blue"))),
				FieldName("_2") -> Taint("yellow")
			)
		)

		assertEquals(Set(Taint("blue"), Taint("yellow")), taintOfBase(f, taint))
	}

	@Test
	def testTaintsInFun3(): Unit = {
		val f = (t : Rep[(Int, Int, String)]) => string_length(t._3) + t._1

		val c1 = Taint("red")
		val c2 = Taint("blue")

		assertEquals(Set(Taint("blue"), Taint("red")), taintOfBase(f, Taint.tupled(c1, c1, c2)))
	}

	@Test
	def testProjectionTaint1(): Unit = {
		val f : Rep[_ => _] = (t : Rep[((Int, Int), Int)]) => make_tuple2((t._1._2 + 1, t._2))

		val taint = Taint(
			"_1" -> Taint("_1" -> Taint("red"), "_2" -> Taint("blue")),
			"_2" -> Taint("yellow")
		)

		assertEquals(Taint("_1" -> Taint("blue"), "_2" -> Taint("yellow")), taintOfProjection(taint, f))
	}

	@Test
	def testProjectionTaint2(): Unit = {
		val f : Rep[_ => _] = (t : Rep[((Int, Int), Int)]) => t._2

		val taint = Taint(
			"_1" -> Taint("_1" -> Taint("red"), "_2" -> Taint("blue")),
			"_2" -> Taint("yellow")
		)

		assertEquals(Taint("yellow"), taintOfProjection(taint, f))
	}

	@Test
	def testProjectionTaint3(): Unit = {
		val f : Rep[_ => _] = (t : Rep[Int]) => make_tuple2((t, __unit(0)))

		val taint = Taint("red")

		assertEquals(Taint("_1" -> Taint("red"), "_2" -> Taint.empty), taintOfProjection(taint, f))
	}

	@Test
	def testProjectionTaint4(): Unit = {
		val f : Rep[_ => _] = (t : Rep[((Int, Int), Int)]) => make_tuple3((t._1._2 + t._2, __unit(0), __unit(1)))

		val taint = Taint(
			"_1" -> Taint("_1" -> Taint("red"), "_2" -> Taint("blue")),
			"_2" -> Taint("yellow")
		)

		assertEquals(Taint("_1" -> Taint.group("blue", "yellow"), "_2" -> Taint.empty, "_3" -> Taint.empty), taintOfProjection(taint, f))
	}



}
