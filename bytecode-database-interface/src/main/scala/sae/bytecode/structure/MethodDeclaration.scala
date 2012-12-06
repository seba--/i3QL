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
package sae.bytecode.structure

import de.tud.cs.st.bat._
import resolved.ObjectType

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 23.08.12
 * Time: 13:08
 */

class MethodDeclaration(val declaringClass: ClassDeclaration,
                        val accessFlags: Int,
                        val name: String,
                        val returnType: de.tud.cs.st.bat.resolved.Type,
                        val parameterTypes: Seq[de.tud.cs.st.bat.resolved.FieldType])
    extends DeclaredClassMember
    with MethodInfo
    with MethodComparison
{
    def receiverType = declaringClassType

    def isPublic = ACC_PUBLIC ∈ accessFlags

    def isProtected = ACC_PROTECTED ∈ accessFlags

    def isPrivate = ACC_PRIVATE ∈ accessFlags

    def isStatic = ACC_STATIC ∈ accessFlags

    def isFinal = ACC_FINAL ∈ accessFlags

    def isSynchronized = ACC_SYNCHRONIZED ∈ accessFlags

    def isBridge = ACC_BRIDGE ∈ accessFlags

    def isVarArgs = ACC_VARARGS ∈ accessFlags

    def isNative = ACC_NATIVE ∈ accessFlags

    def isAbstract = ACC_ABSTRACT ∈ accessFlags

    def isStrict = ACC_STRICT ∈ accessFlags

    def isSynthetic = ACC_SYNTHETIC ∈ accessFlags

}

object MethodDeclaration
{

    def apply(declaringType: ObjectType,
              name: String,
              returnType: de.tud.cs.st.bat.resolved.Type,
              parameterTypes: Seq[de.tud.cs.st.bat.resolved.FieldType]): MethodDeclaration =
        new MethodDeclaration (
            new ClassDeclaration (0, 0, 0, declaringType, None, Seq ()),
            0,
            name,
            returnType,
            parameterTypes
        )

}