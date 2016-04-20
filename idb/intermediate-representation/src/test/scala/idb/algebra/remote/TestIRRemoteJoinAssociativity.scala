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
package idb.algebra.remote

import idb.algebra.TestUtils
import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.algebra.print.{RelationalAlgebraPrintPlan, RelationalAlgebraPrintPlanBasicOperators}
import idb.lms.extensions.ScalaOpsExpOptExtensions
import idb.lms.extensions.operations.StringOpsExpExt
import idb.query.colors.Color
import idb.query.{QueryEnvironment}
import org.junit.Assert._
import org.junit.Test

import scala.virtualization.lms.common.{StringOpsExp, LiftAll}

/**
 *
 * @author Mirko Köhler
 *
 */
class TestIRRemoteJoinAssociativity
    extends RelationalAlgebraIRRemoteJoinAssociativity
    with RelationalAlgebraIRBasicOperators
    with RelationalAlgebraPrintPlanBasicOperators
    with ScalaOpsExpOptExtensions
	with StringOpsExpExt
    with LiftAll
    with TestUtils
{

    // needs binding for printing relation
    override val IR: this.type = this

    override def reset { super.reset }

	override def isPrimitiveType[T](m: Manifest[T]) : Boolean =
		super.isPrimitiveType[T](m)


	@Test
	def testJoinAssociativity1(): Unit = {
		//a >< (b >< c) --> (a >< b) >< c
		implicit val local = QueryEnvironment.Local

		val tableA = table(scala.List.empty[String], color = Color("A"))
		val tableB = table(scala.List.empty[String], color = Color("A"))
		val tableC = table(scala.List.empty[String], color = Color("C"))

		val eqBC = // : List[(Rep[String => Any], Rep[String => Any])] =
			scala.List(
			(fun((b : Rep[String]) => string_substring(b, 1)), fun((c : Rep[String]) => string_substring(c, 0)))
		)

		val eqABC = //: List[(Rep[String => Any], Rep[(String, String) => Any])] =
			scala.List(
			(fun((a : Rep[String]) => infix_toLowerCase(a)), fun((bc : Rep[(String, String)]) => infix_toLowerCase(bc._2))),
			(fun((a : Rep[String]) => string_length(a)), fun((bc : Rep[(String, String)]) => string_length(bc._1)))
		)

		val q = equiJoin(tableA, equiJoin(tableB, tableC, eqBC), eqABC)

	/*	Predef.println("Global Defs Cache ######################")
		globalDefsCache.foreach(Predef.println)
		Predef.println("########################################")
		Predef.println(quoteRelation(q))  */

		q match {
			case Def(Projection(
					Def(EquiJoin(
						Def(EquiJoin(a, b, _)),
						c,
					_)),
				_)) =>

				assertTrue(a == tableA)
				assertTrue(b == tableB)
				assertTrue(c == tableC)

			case _ =>
				fail(s"Wrong query structure : ${quoteRelation(q)}")
		}

	}

	@Test
	def testJoinAssociativity2(): Unit = {
		//(a >< b) >< c --> a >< (b >< c)

		implicit val local = QueryEnvironment.Local

		val tableA = table(scala.List.empty[String], color = Color("A"))
		val tableB = table(scala.List.empty[String], color = Color("C"))
		val tableC = table(scala.List.empty[String], color = Color("C"))

		val eqAB = // : List[(Rep[String => Any], Rep[String => Any])] =
			scala.List(
				(fun((a : Rep[String]) => string_substring(a, 1)), fun((b : Rep[String]) => string_substring(b, 0)))
			)

		val eqABC = //: List[(Rep[String => Any], Rep[(String, String) => Any])] =
			scala.List(
				(fun((ab : Rep[(String, String)]) => infix_toLowerCase(ab._2)), fun((c : Rep[String]) => infix_toLowerCase(c))),
				(fun((ab : Rep[(String, String)]) => string_length(ab._1)), fun((c : Rep[String]) => string_length(c)))
			)

		val q = equiJoin(equiJoin(tableA, tableB, eqAB), tableC, eqABC)

		/*
			Predef.println("Global Defs Cache ######################")
			globalDefsCache.foreach(Predef.println)
			Predef.println("########################################")
			Predef.println(quoteRelation(q))
			*/

		q match {
			case Def(Projection(
					Def(EquiJoin(
						a,
						Def(EquiJoin(b, c, _)),
						_)),
				_)) =>

				assertTrue(a == tableA)
				assertTrue(b == tableB)
				assertTrue(c == tableC)

			case _ =>
				fail(s"Wrong query structure : ${quoteRelation(q)}")
		}

	}

}
