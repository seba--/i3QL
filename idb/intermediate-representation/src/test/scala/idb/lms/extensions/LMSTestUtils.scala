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

import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}
import idb.lms.extensions.print.QuoteFunction
import junit.framework.Assert
import scala.virtualization.lms.common.{StaticDataExp, TupledFunctionsExp, StructExp, ScalaOpsPkgExp}

/**
 *
 * @author Ralf Mitschke
 */
trait LMSTestUtils
    extends ScalaOpsPkgExp
    with StructExp
    with StaticDataExp
    with OptionOpsExp
    with StringOpsExpExt
    with SeqOpsExpExt
    with TupledFunctionsExp
    with FunctionUtils
{

    val printer = new QuoteFunction
    {
        val IR: LMSTestUtils.this.type = LMSTestUtils.this
    }

    def assertEqualFunctions[A1, A2, B1, B2] (a: Rep[A1 => B1], b: Rep[A2 => B2]) {
        val expectedString = printer.quoteFunction (a)
        val actualString = printer.quoteFunction (b)
        val message = "expected:\n<" + expectedString + ">\nbut was:\n<" + actualString + ">"
        if (a != b) {
            Assert.fail (message)
        }
    }


    def assertNotEqualFunctions[A1, A2, B1, B2] (a: Rep[A1 => B1], b: Rep[A2 => B2]) {
        val expectedString = printer.quoteFunction (a)
        val actualString = printer.quoteFunction (b)
        val message = "expected:\n<" + expectedString + ">\nto be different from:\n<" + actualString + ">"
        if (a.equals (b)) {
            Assert.fail (message)
        }
    }
}
