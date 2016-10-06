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

import org.junit.{Ignore, Test}
import scala.virtualization.lms.common.{ScalaOpsPkgExp, LiftAll}
import org.junit.Assert._
import idb.lms.extensions.ScalaOpsExpOptExtensions

/**
 *
 * @author Ralf Mitschke
 */
class TestTupleOpsReduction
    extends LiftAll with ScalaOpsExpOptExtensions with ScalaOpsPkgExp
{

    @Test
    def testTuple2ReduceDirect () {
        val f1 = (x: Rep[Int]) => (x, x > 0)._2
        val f2 = (x: Rep[Int]) => x > 0

        assertSame (fun (f1), fun (f2))

    }

    @Ignore
    @Test
    def testTuple2ReduceFunComposeThenDirect () {
        // TODO should create fun first to be a good test
        val f1 = (x: Rep[Int]) => (x, x > 0)
        val f2 = (x: Rep[(Int, Boolean)]) => x._2
        val f3 = (x: Rep[Int]) => f2 (f1 (x))

        val f4 = (x: Rep[Int]) => x > 0

        assertSame (fun (f3), fun (f4))
    }

    @Ignore
    @Test
    def testTuple2ReduceFunComposeThenEqTest () {
        val f1 = (x: Rep[Int]) => (x, x > 0)
        val f2 = (x: Rep[(Int, Boolean)]) => x._2 == true
        val f3 = (x: Rep[Int]) => f2 (f1 (x))

        val f4 = (x: Rep[Int]) => x > 0 == true

        assertSame (fun (f3), fun (f4))
    }

    @Test
    @Ignore
    def testTuple2ReduceDirectConditional () {
        val f1 = (x: Rep[Int]) => if (x > 0) (x, unit (true)) else (x, unit (false))._2
        val f2 = (x: Rep[Int]) => if (x > 0) unit (true) else unit (false)

        assertSame (fun (f1), fun (f2))
    }

}
