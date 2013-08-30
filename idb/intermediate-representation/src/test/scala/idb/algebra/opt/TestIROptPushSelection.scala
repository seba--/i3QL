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
import idb.lms.extensions.equivalence.ScalaOpsPkgExpAlphaEquivalence
import idb.lms.extensions.operations.OptionOpsExp
import org.junit.Assert._
import org.junit.{Ignore, Test}
import scala.virtualization.lms.common.{StaticDataExp, TupledFunctionsExp, StructExp, LiftAll}

/**
 *
 * @author Ralf Mitschke
 *
 */
class TestIROptPushSelection
    extends RelationalAlgebraIROptPushSelection
    with RelationalAlgebraIRBasicOperators
    with RelationalAlgebraPrintPlanBasicOperators
    with ScalaOpsPkgExpAlphaEquivalence
    with StructExp
    with StaticDataExp
    with OptionOpsExp
    with TupledFunctionsExp
    with LiftAll
    with TestUtils
{

    // needs binding for printing relation
    override val IR: this.type = this

    override def reset { super.reset }

    @Test
    def testSelectionOverProjectionSimpleInt () {
        val f1 = fun ((x: Rep[Int]) => x + 2)
        val f2 = fun ((x: Rep[Int]) => x > 0)

        val expA = selection (projection (emptyRelation[Int](), f1), f2)

        val f3 = (x: Rep[Int]) => f2 (f1 (x)) // x > -2

        val expB = projection (selection (emptyRelation[Int](), f3), f1)

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }

    @Test
    def testSelectionOverProjectionConditionalInt () {
        val f1 = fun ((x: Rep[Int]) => if (x > 0) unit (true) else unit (false))
        val f2 = fun ((x: Rep[Boolean]) => x)

        val expA = selection (projection (emptyRelation[Int](), f1), f2)

        val f3 = (x: Rep[Int]) => f2 (f1 (x)) // if (x > 0) unit (true) else unit (false) // x > 0

        val expB = projection (selection (emptyRelation[Int](), f3), f1)

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }

    @Ignore // needs tupled functions
    @Test
    def testSelectionOverProjectionSimpleTuple () {
        val f1 = fun ((x: Rep[Int]) => (x, x > 0))

        val f2 = fun ((x: Rep[(Int, Boolean)]) => x._2)

        val expA = selection (projection (emptyRelation[Int](), f1), f2)

        val f3 = (x: Rep[Int]) => f2 (f1 (x)) // x > 0

        val expB = projection (selection (emptyRelation[Int](), f3), f1)

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }

    @Ignore // needs tupled functions
    @Test
    def testSelectionOverProjectionConditionalTuple () {
        val f1 = fun ((x: Rep[Int]) => if (x > 0) (x, unit (true)) else (x, unit (false)))
        val f2 = fun ((x: Rep[(Int, Boolean)]) => x._2)

        val expA = selection (projection (emptyRelation[Int](), f1), f2)

        val f3 = (x: Rep[Int]) => f2 (f1 (x)) //x > 0 == true

        //val f4 = (x: Rep[Int]) => (if (x > 0) (x, unit (true)) else (x, unit (false)))._2

        //val f4 = (x: Rep[Int]) => (if (x > 0) unit (true) else unit (false)) == true

        val expB = projection (selection (emptyRelation[Int](), f3), f1)

        assertEquals (quoteRelation (expB), quoteRelation (expA))
        assertEquals (expB, expA)
    }

}
