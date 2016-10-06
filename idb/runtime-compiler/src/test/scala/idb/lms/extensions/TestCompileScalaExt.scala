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

import idb.lms.extensions.functions.TupledFunctionsExpDynamicLambda

import scala.language.reflectiveCalls
import org.junit.Test
import org.junit.Assert._
import scala.virtualization.lms.common._
import idb.schema.university.{StudentSchema, Student, University}
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRRecursiveOperators, RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRBasicOperators}
import idb.algebra.compiler.{RelationalAlgebraGenBasicOperatorsAsIncremental, RelationalAlgebraSAEBinding}
import idb.lms.extensions.lifiting.LiftEverything

/**
 *
 * @author Ralf Mitschke
 */
class TestCompileScalaExt
{

    @Test
    def testCompileFunction1 () {
        val prog = new RelationalAlgebraIRBasicOperators
			with RelationalAlgebraIRSetTheoryOperators
			with RelationalAlgebraIRRecursiveOperators
			with RelationalAlgebraIRAggregationOperators
            with RelationalAlgebraSAEBinding
            with ScalaOpsPkgExp
            with University
            with LiftEverything
        {
            def isSally (s: Rep[Student]) = s.firstName == "Sally"
        }

        val compiler = new RelationalAlgebraGenBasicOperatorsAsIncremental with ScalaCodeGenPkg with ScalaGenStruct
        {
            val IR: prog.type = prog

            silent = true
        }

        val isSally = compiler.compileFunction (prog.isSally)

        assertTrue (isSally (Student (1, "Sally", "Fields")))
        assertTrue (isSally (Student (2, "Sally", "Moore")))
        assertFalse (isSally (Student (3, "John", "Moore")))
        assertFalse (isSally (Student (4, "Sall", "White")))
    }

    @Test
    def testCompileFunction2 () {
        val prog = new RelationalAlgebraIRBasicOperators
			with RelationalAlgebraIRSetTheoryOperators
			with RelationalAlgebraIRRecursiveOperators
			with RelationalAlgebraIRAggregationOperators
            with RelationalAlgebraSAEBinding
            with ScalaOpsPkgExp
            with University
			with TupledFunctionsExpDynamicLambda
            with TupledFunctionsExp
            with LiftAll
        {
            def matchesString (s: Rep[Student], v: Rep[String]) = s.firstName == v
        }

        val compiler = new RelationalAlgebraGenBasicOperatorsAsIncremental with ScalaCodeGenPkg with ScalaGenTupledFunctions with ScalaGenStruct
        {
            val IR: prog.type = prog

            silent = true
        }

        val matchesString = compiler.compileFunctionApplied (prog.fun(prog.matchesString _))

        assertTrue (matchesString ((Student (1, "Sally", "Fields"), "Sally")))
        assertTrue (matchesString ((Student (2, "Sally", "Moore"), "Sally")))
        assertFalse (matchesString ((Student (3, "John", "Moore"), "Sally")))
        assertFalse (matchesString ((Student (4, "Sall", "White"), "Sally")))
    }

    @Test
    def testCompileFunction1WithDynamicManifests () {
        val prog = new RelationalAlgebraIRBasicOperators
			with RelationalAlgebraIRSetTheoryOperators
			with RelationalAlgebraIRRecursiveOperators
			with RelationalAlgebraIRAggregationOperators
            with RelationalAlgebraSAEBinding
            with ScalaOpsPkgExp
            with University
            with LiftAll
        {
            def isSally (s: Rep[Student]) = s.firstName == "Sally"
        }

        val compiler = new RelationalAlgebraGenBasicOperatorsAsIncremental with ScalaCodeGenPkg with ScalaGenStruct
        {
            val IR: prog.type = prog

            silent = false
        }

        val isSally = compiler.compileFunctionWithDynamicManifests (prog.fun (prog.isSally _))

        assertTrue (isSally (Student (1, "Sally", "Fields")))
        assertTrue (isSally (Student (2, "Sally", "Moore")))
        assertFalse (isSally (Student (3, "John", "Moore")))
        assertFalse (isSally (Student (4, "Sall", "White")))
    }

    @Test
    def testCompileFunction2WithDynamicManifests () {
        val prog = new RelationalAlgebraIRBasicOperators
			with RelationalAlgebraIRSetTheoryOperators
			with RelationalAlgebraIRRecursiveOperators
			with RelationalAlgebraIRAggregationOperators
            with RelationalAlgebraSAEBinding
            with ScalaOpsPkgExp
            with University
			with TupledFunctionsExpDynamicLambda
            with TupledFunctionsExp
            with LiftAll
        {
            def matchesString (s: Rep[Student], v: Rep[String]) = s.firstName == v
        }

        val compiler = new RelationalAlgebraGenBasicOperatorsAsIncremental with ScalaCodeGenPkg with ScalaGenTupledFunctions with ScalaGenStruct
        {
            val IR: prog.type = prog

            silent = true
        }

        val matchesString = compiler.compileFunctionWithDynamicManifests (prog.fun(prog.matchesString _))

        assertTrue (matchesString ((Student (1, "Sally", "Fields"), "Sally")))
        assertTrue (matchesString ((Student (2, "Sally", "Moore"), "Sally")))
        assertFalse (matchesString ((Student (3, "John", "Moore"), "Sally")))
        assertFalse (matchesString ((Student (4, "Sall", "White"), "Sally")))
    }

    @Test
    def testCompileExplicitToString () {
        val prog = new RelationalAlgebraIRBasicOperators
			with RelationalAlgebraIRSetTheoryOperators
			with RelationalAlgebraIRRecursiveOperators
			with RelationalAlgebraIRAggregationOperators
            with RelationalAlgebraSAEBinding
            with ScalaOpsPkgExp
            with University
        {
            def getNumberAsString (s: Rep[Student]) : Rep[String] = s.matriculationNumber.toString()  // TODO toString without parenthesis does not work!!
        }

        val compiler = new RelationalAlgebraGenBasicOperatorsAsIncremental with ScalaCodeGenPkg with ScalaGenStruct
        {
            val IR: prog.type = prog

            silent = false
        }

        val getNumberAsString = compiler.compileFunctionWithDynamicManifests (prog.fun (prog.getNumberAsString _))

        assertEquals ("1", getNumberAsString (Student (1, "Sally", "Fields")))
        assertEquals ("2", getNumberAsString (Student (2, "Sally", "Moore")))
        assertEquals ("3333", getNumberAsString (Student (3333, "John", "Moore")))
        assertEquals ("423123", getNumberAsString (Student (423123, "Sall", "White")))
    }
}
