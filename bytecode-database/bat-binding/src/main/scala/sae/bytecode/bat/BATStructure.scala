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
package sae.bytecode.bat

import sae.bytecode.BytecodeStructure
import de.tud.cs.st.bat._

/**
 *
 * @author Ralf Mitschke
 */
trait BATStructure
    extends BytecodeStructure
    with BATStructureUtils
{


    case class ClassDeclaration (
        minorVersion: Int,
        majorVersion: Int,
        accessFlags: Int,
        classType: ObjectType,
        superClass: Option[ObjectType],
        interfaces: Seq[ObjectType]
    ) extends super.ClassDeclaration
    {

        def isAnnotation = BATStructure.this.isAnnotation (this)

        def isClass = BATStructure.this.isClass (this)

        def isEnum = BATStructure.this.isEnum (this)

        def isInterface = BATStructure.this.isInterface (this)

        def isPublic = ACC_PUBLIC ∈ accessFlags

        def isDefault = !(ACC_PUBLIC ∈ accessFlags)

        def isFinal = ACC_FINAL ∈ accessFlags

        def isAbstract = ACC_ABSTRACT ∈ accessFlags

        def isSynthetic = ACC_SYNTHETIC ∈ accessFlags

    }

    case class MethodDeclaration (
        declaringClass: ClassDeclaration,
        accessFlags: Int,
        name: String,
        returnType: Type,
        parameterTypes: Seq[FieldType]
    )
        extends super.MethodDeclaration
    {
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

    case class FieldDeclaration (
        declaringClass: ClassDeclaration,
        accessFlags: Int,
        name: String,
        fieldType: FieldType
    )
        extends super.FieldDeclaration
    {
        def isPublic = ACC_PUBLIC ∈ accessFlags

        def isProtected = ACC_PROTECTED ∈ accessFlags

        def isPrivate = ACC_PRIVATE ∈ accessFlags

        def isStatic = ACC_STATIC ∈ accessFlags

        def isFinal = ACC_FINAL ∈ accessFlags

        def isTransient = ACC_TRANSIENT ∈ accessFlags

        def isVolatile = ACC_VOLATILE ∈ accessFlags

        def isEnum = ACC_ENUM ∈ accessFlags

        def isSynthetic = ACC_SYNTHETIC ∈ accessFlags
    }


    type ExceptionHandler = de.tud.cs.st.bat.resolved.ExceptionHandler

    case class CodeAttribute (
        declaringMethod: MethodDeclaration,
        codeLength: Int,
        maxStack: Int,
        maxLocals: Int,
        exceptionHandlers: Seq[ExceptionHandler]
    ) extends super.CodeAttribute


    case class InnerClassAttribute (
        declaringClass: ClassDeclaration,
        innerClassType: ObjectType,
        outerClassType: Option[ObjectType],
        innerName: Option[String],
        innerClassAccessFlags: Int
    ) extends super.InnerClassAttribute


    case class EnclosingMethodAttribute (
        declaringClass: ClassDeclaration,
        innerClassType: ObjectType,
        name: Option[String],
        parameterTypes: Option[Seq[FieldType]],
        returnType: Option[Type]
    ) extends super.EnclosingMethodAttribute

}
