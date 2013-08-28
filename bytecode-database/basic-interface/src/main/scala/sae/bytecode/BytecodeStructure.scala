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
package sae.bytecode


/**
 *
 * @author Ralf Mitschke
 */
trait BytecodeStructure
    extends BytecodeTypes
{

    trait ClassDeclaration
    {
        def minorVersion: Int

        def majorVersion: Int

        def accessFlags: Int

        def classType: ObjectType

        def superClass: Option[ObjectType]

        def interfaces: Seq[ObjectType]

        def isAnnotation: Boolean = BytecodeStructure.this.isAnnotation (this)

        def isClass: Boolean = BytecodeStructure.this.isClass (this)

        def isEnum: Boolean = BytecodeStructure.this.isEnum (this)

        def isInterface: Boolean = BytecodeStructure.this.isInterface (this)

        def isPublic: Boolean = BytecodeStructure.this.isPublic (this)

        def isDefault: Boolean = BytecodeStructure.this.isDefault (this)

        def isFinal: Boolean = BytecodeStructure.this.isFinal (this)

        def isAbstract: Boolean = BytecodeStructure.this.isAbstract (this)

        def isSynthetic: Boolean = BytecodeStructure.this.isSynthetic (this)
    }


    trait DeclaredClassMember
    {
        def declaringClass: ClassDeclaration

        def declaringType: ObjectType = declaringClass.classType

        def name: String

        def accessFlags: Int

        def isPublic: Boolean = BytecodeStructure.this.isPublic (this)

        def isProtected: Boolean = BytecodeStructure.this.isProtected (this)

        def isPrivate: Boolean = BytecodeStructure.this.isPrivate (this)

        def isStatic: Boolean = BytecodeStructure.this.isStatic (this)

        def isFinal: Boolean = BytecodeStructure.this.isFinal (this)
    }


    trait MethodInfo
    {

        def receiverType: ReferenceType

        def name: String

        def returnType: Type

        def parameterTypes: Seq[FieldType]
    }

    trait MethodDeclaration
        extends DeclaredClassMember
        with MethodInfo
    {
        def declaringClass: ClassDeclaration

        def returnType: Type

        def parameterTypes: Seq[FieldType]

        def receiverType = declaringType

        def isSynchronized: Boolean = BytecodeStructure.this.isSynchronized (this)

        def isBridge: Boolean = BytecodeStructure.this.isBridge (this)

        def isVarArgs: Boolean = BytecodeStructure.this.isVarArgs (this)

        def isNative: Boolean = BytecodeStructure.this.isNative (this)

        def isAbstract: Boolean = BytecodeStructure.this.isAbstract (this)

        def isStrict: Boolean = BytecodeStructure.this.isStrict (this)

        def isSynthetic: Boolean = BytecodeStructure.this.isSynthetic (this)
    }


    trait FieldInfo
    {
        def declaringType: ObjectType

        def name: String

        def fieldType: FieldType

    }


    trait FieldDeclaration
        extends DeclaredClassMember
        with FieldInfo
    {
        def isTransient: Boolean = BytecodeStructure.this.isTransient (this)

        def isVolatile: Boolean = BytecodeStructure.this.isVolatile (this)

        def isEnum: Boolean = BytecodeStructure.this.isEnum (this)

        def isSynthetic: Boolean = BytecodeStructure.this.isSynthetic (this)
    }

    type ExceptionHandler

    trait CodeAttribute
    {
        def declaringMethod: MethodDeclaration

        def codeLength: Int

        def maxStack: Int

        def maxLocals: Int

        def exceptionHandlers: Seq[ExceptionHandler]
    }


    trait EnclosingMethodAttribute
    {
        def declaringClass: ClassDeclaration

        def name: Option[String]

        def parameterTypes: Option[Seq[FieldType]]

        def returnType: Option[Type]

        def innerClassType: ObjectType
    }


    trait InnerClassAttribute
    {
        def declaringClass: ClassDeclaration

        def innerClassType: ObjectType

        def outerClassType: Option[ObjectType]

        def innerName: Option[String]

        def innerClassAccessFlags: Int
    }

    trait Instruction
    {
        def declaringMethod: MethodDeclaration

        def programCounter: Int

        def sequenceIndex: Int

        def opcode: Short

        def nextProgramCounter: Int

        def mnemonic: String = this.getClass.getSimpleName
    }


    def isAnnotation (classDeclaration: ClassDeclaration): Boolean

    def isClass (classDeclaration: ClassDeclaration): Boolean

    def isEnum (classDeclaration: ClassDeclaration): Boolean

    def isInterface (classDeclaration: ClassDeclaration): Boolean

    def isPublic (classDeclaration: ClassDeclaration): Boolean

    def isDefault (classDeclaration: ClassDeclaration): Boolean

    def isFinal (classDeclaration: ClassDeclaration): Boolean

    def isAbstract (classDeclaration: ClassDeclaration): Boolean

    def isSynthetic (classDeclaration: ClassDeclaration): Boolean

    def isPublic (declaredClassMember: DeclaredClassMember): Boolean

    def isProtected (declaredClassMember: DeclaredClassMember): Boolean

    def isPrivate (declaredClassMember: DeclaredClassMember): Boolean

    def isStatic (declaredClassMember: DeclaredClassMember): Boolean

    def isFinal (declaredClassMember: DeclaredClassMember): Boolean

    def isSynchronized (methodDeclaration: MethodDeclaration): Boolean

    def isBridge (methodDeclaration: MethodDeclaration): Boolean

    def isVarArgs (methodDeclaration: MethodDeclaration): Boolean

    def isNative (methodDeclaration: MethodDeclaration): Boolean

    def isAbstract (methodDeclaration: MethodDeclaration): Boolean

    def isStrict (methodDeclaration: MethodDeclaration): Boolean

    def isSynthetic (methodDeclaration: MethodDeclaration): Boolean

    def isTransient (fieldDeclaration: FieldDeclaration): Boolean

    def isVolatile (fieldDeclaration: FieldDeclaration): Boolean

    def isEnum (fieldDeclaration: FieldDeclaration): Boolean

    def isSynthetic (fieldDeclaration: FieldDeclaration): Boolean

}