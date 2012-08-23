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

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 23.08.12
 * Time: 13:08
 */

case class MethodDeclarationInfo(accessFlags: Int,
                      name: String,
                      returnType: de.tud.cs.st.bat.resolved.Type,
                      parameterTypes: Seq[de.tud.cs.st.bat.resolved.FieldType])
{
    def isPublic = MethodDeclarationInfo.isPublic (this)

    def isProtected = MethodDeclarationInfo.isProtected (this)

    def isPrivate = MethodDeclarationInfo.isPrivate (this)

    def isStatic = MethodDeclarationInfo.isStatic (this)

    def isFinal = MethodDeclarationInfo.isFinal (this)

    def isSynchronized = MethodDeclarationInfo.isSynchronized (this)

    def isBridge = MethodDeclarationInfo.isBridge (this)

    def isVarArgs = MethodDeclarationInfo.isVarArgs (this)

    def isNative = MethodDeclarationInfo.isNative (this)

    def isAbstract = MethodDeclarationInfo.isAbstract (this)

    def isStrict = MethodDeclarationInfo.isStrict (this)
}

object MethodDeclarationInfo
{


    def isPublic(methodDeclaration: MethodDeclarationInfo) =
        ACC_PUBLIC ∈ methodDeclaration.accessFlags

    def isProtected(methodDeclaration: MethodDeclarationInfo) =
        ACC_PROTECTED ∈ methodDeclaration.accessFlags

    def isPrivate(methodDeclaration: MethodDeclarationInfo) =
        ACC_PRIVATE ∈ methodDeclaration.accessFlags

    def isStatic(methodDeclaration: MethodDeclarationInfo) =
        ACC_STATIC ∈ methodDeclaration.accessFlags

    def isFinal(methodDeclaration: MethodDeclarationInfo) =
        ACC_FINAL ∈ methodDeclaration.accessFlags

    def isSynchronized(methodDeclaration: MethodDeclarationInfo) =
        ACC_SYNCHRONIZED ∈ methodDeclaration.accessFlags

    def isBridge(methodDeclaration: MethodDeclarationInfo) =
        ACC_BRIDGE ∈ methodDeclaration.accessFlags

    def isVarArgs(methodDeclaration: MethodDeclarationInfo) =
        ACC_VARARGS ∈ methodDeclaration.accessFlags

    def isNative(methodDeclaration: MethodDeclarationInfo) =
        ACC_NATIVE ∈ methodDeclaration.accessFlags

    def isAbstract(methodDeclaration: MethodDeclarationInfo) =
        ACC_ABSTRACT ∈ methodDeclaration.accessFlags

    def isStrict(methodDeclaration: MethodDeclarationInfo) =
        ACC_STRICT ∈ methodDeclaration.accessFlags

}
