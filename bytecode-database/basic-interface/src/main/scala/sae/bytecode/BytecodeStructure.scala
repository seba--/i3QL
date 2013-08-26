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

        def isAnnotation: Boolean

        def isClass: Boolean

        def isEnum: Boolean

        def isInterface: Boolean

        def isPublic: Boolean

        def isDefault: Boolean

        def isFinal: Boolean

        def isAbstract: Boolean

        def isSynthetic: Boolean
    }


    trait DeclaredClassMember
    {
        def declaringClass: ClassDeclaration

        def declaringType: ObjectType = declaringClass.classType

        def name: String

        def accessFlags: Int

        def isPublic: Boolean

        def isProtected: Boolean

        def isPrivate: Boolean

        def isStatic: Boolean

        def isFinal: Boolean
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

        def isSynchronized: Boolean

        def isBridge: Boolean

        def isVarArgs: Boolean

        def isNative: Boolean

        def isAbstract: Boolean

        def isStrict: Boolean

        def isSynthetic: Boolean
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
        def isTransient : Boolean

        def isVolatile : Boolean

        def isEnum : Boolean

        def isSynthetic : Boolean
    }

    trait CodeAttribute {
        def declaringMethod : MethodDeclaration

        def maxStack: Int

        def maxLocals: Int

        def exceptionHandlers: Seq[ObjectType]
    }


    trait EnclosingMethodAttribute{
        def declaringType: ObjectType

        def name: Option[String]

        def parameterTypes: Option[Seq[FieldType]]

        def returnType: Option[Type]

        def innerClassType: ObjectType
    }


    trait InnerClassAttribute {
        def innerClassType: ObjectType

        def outerClassType: Option[ObjectType]

        def innerName: Option[String]

        def innerClassAccessFlags: Int
    }
}