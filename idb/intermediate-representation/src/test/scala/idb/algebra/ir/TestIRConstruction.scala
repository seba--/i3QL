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
package idb.algebra.ir

import idb.algebra.TestUtils
import idb.query.QueryEnvironment
import org.junit.Assert._
import org.junit.Test
import scala.virtualization.lms.common.{ScalaOpsPkgExp, LiftAll}

/**
 *
 * @author Ralf Mitschke
 *
 */

class TestIRConstruction
    extends RelationalAlgebraIRBasicOperators
    with LiftAll
    with ScalaOpsPkgExp
    with TestUtils
{
    @Test
    def testSelection () {
		implicit val local = QueryEnvironment.Local

        val f = fun ((x: Rep[Int]) => x > 0)
        val exp = selection (emptyRelation[Int], f)
        val s = syms (exp)(0)
        val d = findDefinition (s) match {
            case Some (TP (_, rhs)) => rhs
            case _ => null
        }

        assertEquals (
            Selection (emptyRelation[Int], f),
            d
        )
    }


    @Test
    def testCommonSubExpressionWithSelection () {
		implicit val local = QueryEnvironment.Local

		val f = fun ((x: Rep[Int]) => x > 0)
        val exp1 = selection (emptyRelation[Int], f)
        val exp2 = selection (emptyRelation[Int], f)

        assertEquals (
            exp1,
            exp2
        )
    }

  /* @Test
     def testLocalIncrementTable () {
        val e = emptyRelation[Data]()

        assertEquals (
            QueryTable (scala.List.empty[Data], isSet = false, isIncrementLocal = true, isMaterialized = false),
            e
        )
    } */
}
