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
package sae.analyses.findbugs.selected

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 *
 * @author Ralf Mitschke
 *
 */

object CN_IDIOM
	extends (BytecodeDatabase => Relation[BytecodeDatabase#ObjectType])
{
    def apply (database: BytecodeDatabase): Relation[BytecodeDatabase#ObjectType] = {
        import database._

        SELECT ((_: Rep[Inheritance]).subType) FROM interfaceInheritance WHERE ((t: Rep[Inheritance]) =>
            NOT (t.declaringClass.isInterface) AND
                NOT (t.declaringClass.isAbstract) AND
                t.superType == ObjectType ("java/lang/Cloneable") AND
                NOT (
                    EXISTS (
                        SELECT (*) FROM methodDeclarations WHERE ((m: Rep[MethodDeclaration]) =>
                            NOT (m.isAbstract) AND
                                NOT (m.isSynthetic) AND
                                m.isPublic AND
                                m.name == "clone" AND
                                // This is actually never checked by FindBugs
                                //m.returnType == ObjectType ("java/lang/Object") AND
                                m.parameterTypes == Nil AND
                                t.subType == m.declaringType
                            )
                    )
                ) AND
                NOT (
                    EXISTS (
                        SELECT (*) FROM methodInvocationInstructions WHERE ((m: Rep[MethodInvocationInstruction]) =>
                            m.methodInfo.name == "clone" AND
                                // This is actually never checked by FindBugs
                                //m.returnType == ObjectType ("java/lang/Object") AND
                                m.methodInfo.parameterTypes == Nil AND
                                t.subType == m.declaringMethod.declaringType
                            )
                    )
                )
            )
    }

}
