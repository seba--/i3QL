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
package idb.algebra.compiler

import idb.query.QueryEnvironment

import scala.language.reflectiveCalls
import org.junit.Test
import org.junit.Assert._
import scala.virtualization.lms.common._
import idb.schema.university.{Student, University}
import idb.SetTable
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRRecursiveOperators, RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRBasicOperators}
import idb.lms.extensions.lifiting.LiftEverything

/**
 *
 * @author Ralf Mitschke
 *
 */
class TestIRGenBasicOperatorsAsIncremental
{



    @Test
    def testConstructSelection () {
		implicit val local = QueryEnvironment.Local

		val base = new SetTable[Student]

        val prog = new RelationalAlgebraIRBasicOperators
            with RelationalAlgebraIRSetTheoryOperators
            with RelationalAlgebraIRRecursiveOperators
            with RelationalAlgebraIRAggregationOperators
            with RelationalAlgebraSAEBinding
            with ScalaOpsPkgExp
            with University
            with LiftEverything
        {
            val query = selection (table (base), (s: Rep[Student]) => s.firstName == "Sally")
        }

        val compiler = new RelationalAlgebraGenBasicOperatorsAsIncremental with ScalaCodeGenPkg with ScalaGenStruct
        {
            val IR: prog.type = prog

            silent = true
        }

        val result = compiler.compile (prog.query).asMaterialized

        val sally = Student (1, "Sally", "Fields")
        val bob = Student (2, "Bob", "Martin")

        base.add (sally)
        base.add (bob)

        assertEquals (
            List (sally),
            result.asList
        )

        base.remove (sally)

        assertEquals (
            Nil,
            result.asList
        )
    }

}
