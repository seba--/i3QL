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
package sae.bytecode.asm

import sae.bytecode.BytecodeStructureConstructors

/**
 *
 * @author Ralf Mitschke
 */
trait ASMStructureConstructors
    extends BytecodeStructureConstructors
    with ASMStructure
{

    /*
    def ClassDeclaration (minorVersion: Int,
        majorVersion: Int,
        accessFlags: Int,
        classType: ObjectType,
        superClass: Option[ObjectType],
        interfaces: Seq[ObjectType]
    ): ClassDeclaration =
        super.ClassDeclaration (
            minorVersion,
            majorVersion,
            accessFlags,
            classType,
            superClass,
            interfaces
        )

    def MethodInfo (
        receiverType: ReferenceType,
        name: String,
        returnType: Type,
        parameterTypes: Seq[FieldType]
    ): MethodInfo =
        super.MethodInfo (
            receiverType,
            name,
            returnType,
            parameterTypes
        )


    def MethodDeclaration (
        declaringClass: ClassDeclaration,
        accessFlags: Int,
        name: String,
        returnType: Type,
        parameterTypes: Seq[FieldType]
    ): MethodDeclaration =
        super.MethodDeclaration (
            declaringClass,
            accessFlags,
            name,
            returnType,
            parameterTypes
        )


    def FieldInfo (
        declaringType: ObjectType,
        name: String,
        fieldType: FieldType
    ): FieldInfo =
        super.FieldInfo (
            declaringType,
            name,
            fieldType
        )


    def FieldDeclaration (
        declaringClass: ClassDeclaration,
        accessFlags: Int,
        name: String,
        fieldType: FieldType
    ): FieldDeclaration =
        super.FieldDeclaration (
            declaringClass,
            accessFlags,
            name,
            fieldType
        )


    def CodeAttribute (
        declaringMethod: MethodDeclaration,
        codeLength: Int,
        maxStack: Int,
        maxLocals: Int,
        exceptionHandlers: Seq[ExceptionHandler]
    ): CodeAttribute =
        super.CodeAttribute (
            declaringMethod,
            codeLength,
            maxStack,
            maxLocals,
            exceptionHandlers
        )


    def EnclosingMethodAttribute (
        declaringClass: ClassDeclaration,
        innerClassType: ObjectType,
        name: Option[String],
        parameterTypes: Option[Seq[FieldType]],
        returnType: Option[Type]
    ): EnclosingMethodAttribute =
        super.EnclosingMethodAttribute (
            declaringClass,
            innerClassType,
            name,
            parameterTypes,
            returnType
        )


    def InnerClassAttribute (
        declaringClass: ClassDeclaration,
        innerClassType: ObjectType,
        outerClassType: Option[ObjectType],
        innerName: Option[String],
        innerClassAccessFlags: Int
    ): InnerClassAttribute =
        super.InnerClassAttribute (
            declaringClass,
            innerClassType,
            outerClassType,
            innerName,
            innerClassAccessFlags
        )
    */
}
