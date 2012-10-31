/* License (BSD Style License):
*  Copyright (c) 2009, 2012
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
package sae.analyses.findbugs.random.relational

import instructions.InstructionInfo
import sae.Relation
import sae.functions.Count


/**
 * Finalize just calls super.finalize.
 *
 * @author Ralf Mitschke
 *         BAT version by Michael Eichberg
 */
object FI_USELESS
    //extends (BytecodeDatabase => Relation[MethodDeclaration])
{

      /*
    def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
        import database._

        val invokeSpecial /*: Relation[INVOKESPECIAL] */ = SELECT ((_: InstructionInfo).asInstanceOf[INVOKESPECIAL]) FROM instructions WHERE (_.isInstanceOf[INVOKESPECIAL])

        val subselect = SELECT (*) FROM invokeSpecial WHERE
            (_.name == "finalize") AND
            (_.returnType == void) AND
            (_.parameterTypes == Nil) AND
            (declaringMethod === (identity(_: MethodDeclaration)))

        SELECT (*) FROM (methodDeclarations) WHERE
            (_.name == "finalize") AND
            (_.returnType == void) AND
            (_.parameterTypes == Nil) AND
            EXISTS (
                SELECT (*) FROM invokeSpecial WHERE
                    (_.name == "finalize") AND
                    (_.returnType == void) AND
                    (_.parameterTypes == Nil) AND
                    (declaringMethod === (_: MethodDeclaration))
            ) AND
            (5 === (SELECT COUNT (*) FROM instructions WHERE (declaringMethod === (_: MethodDeclaration))))
    }
    */

    def foo {
        val c = count(null)
    }

    def count(instructions :Relation[InstructionInfo]) : Relation[Some[Int]]  = {
        import sae.syntax.RelationalAlgebraSyntax._
        γ(instructions, Count[InstructionInfo]())
    }

}
