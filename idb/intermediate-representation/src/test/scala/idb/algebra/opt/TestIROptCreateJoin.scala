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

import idb.algebra.TestUtils
import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.algebra.print.RelationalAlgebraPrintPlanBasicOperators
import idb.lms.extensions.ScalaOpsExpOptExtensions
import idb.query.QueryEnvironment
import org.junit.Assert._
import org.junit.Test

import scala.virtualization.lms.common.LiftAll

/**
 *
 * @author Mirko Köhler
 *
 */
class TestIROptCreateJoin
    extends RelationalAlgebraIROptCreateJoin
    with RelationalAlgebraIRBasicOperators
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
    def testCreateEquiJoin () {
		implicit val local = QueryEnvironment.Local

		val selectionFunc = (t : Rep[(Int, Int)]) => t._1 == t._2

        val expA = selection (crossProduct (emptyRelation[Int], emptyRelation[Int]), selectionFunc)

        val f1 : Rep[Int => Any] = (x: Rep[Int]) => x
        val expB = equiJoin(emptyRelation[Int], emptyRelation[Int], scala.List((f1, f1)))

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }

   	@Test
	def testCreateEqualityFunctions1(): Unit = {
		implicit val local = QueryEnvironment.Local

		val f = (t : Rep[(Int, Int)]) => t._1 + 1 == t._2

		val f1 : Rep[Int => Any] = (i : Rep[Int]) => i + 1
		val f2 : Rep[Int => Any] = (i : Rep[Int]) => i

		assertEquals (
			(f1, f2),
			createEqualityFunctions(f)
		)
	}

	@Test
	def testCreateEqualityFunctions2(): Unit = {
		implicit val local = QueryEnvironment.Local

		val f = (i : Rep[Int], j : Rep[Int]) => i + 1 == j

		val f1 : Rep[Int => Any] = (i : Rep[Int]) => i + 1
		val f2 : Rep[Int => Any] = (i : Rep[Int]) => i

		assertEquals (
			(f1, f2),
			createEqualityFunctions(f)
		)
	}
}
