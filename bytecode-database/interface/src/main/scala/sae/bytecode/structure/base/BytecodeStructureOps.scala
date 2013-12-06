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
package sae.bytecode.structure.base

import scala.language.implicitConversions
import scala.virtualization.lms.common.{Base, StructExp}
import sae.bytecode.types.BytecodeTypeManifests

/**
 *
 * @author Ralf Mitschke
 */
trait BytecodeStructureOps
    extends BytecodeStructure
{

    val IR: Base

    import IR.Rep

    implicit def classDeclarationToInfixOps (c: Rep[ClassDeclaration]): ClassDeclarationInfixOps

    implicit def fieldInfoToInfixOps (f: Rep[FieldInfo]) : FieldInfoInfixOps

    implicit def fieldDeclarationToInfixOps (f: Rep[FieldDeclaration]) : FieldDeclarationInfixOps

    implicit def fieldDeclarationToDeclaredClassMemberInfixOps (f: Rep[FieldDeclaration]): DeclaredClassMemberInfixOps

    implicit def methodDeclarationToDeclaredClassMemberInfixOps (m: Rep[MethodDeclaration]): DeclaredClassMemberInfixOps

    implicit def methodDeclarationToInfixOps (m: Rep[MethodDeclaration]) : MethodDeclarationInfixOps

    implicit def methodInfoToInfixOps (m: Rep[MethodInfo]): MethodInfoInfixOps

    implicit def codeAttributeToInfixOps (c: Rep[CodeAttribute]) : CodeAttributeInfixOps

    implicit def exceptionHandlerToInfixOps (e: Rep[ExceptionHandler]) : ExceptionHandlerInfixOps

    implicit def innerClassAttributeToInfixOps (i: Rep[InnerClassAttribute]) : InnerClassAttributeInfixOps

    implicit def enclosingMethodAttributeToInfixOps (e: Rep[EnclosingMethodAttribute]) : EnclosingMethodAttributeInfixOps


    trait ClassDeclarationInfixOps
    {

        def minorVersion: Rep[Int]

        def majorVersion: Rep[Int]

        def accessFlags: Rep[Int]

        def classType: Rep[ObjectType]

        def superType: Rep[Option[ObjectType]]

        def interfaces: Rep[Seq[ObjectType]]

        // Note that the following are rather derived values and not technically fields
        // But the code generation will just make them o.fieldName calls, which are valid
        def isAnnotation: Rep[Boolean]

        def isClass: Rep[Boolean]

        def isEnum: Rep[Boolean]

        def isInterface: Rep[Boolean]

        def isPublic: Rep[Boolean]

        def isDefault: Rep[Boolean]

        def isFinal: Rep[Boolean]

        def isAbstract: Rep[Boolean]

        def isSynthetic: Rep[Boolean]

    }

    trait DeclaredClassMemberInfixOps
    {

        def declaringClass: Rep[ClassDeclaration]

        def declaringType: Rep[ObjectType]

        def accessFlags: Rep[Int]

        def name: Rep[String]

        def isPublic: Rep[Boolean]

        def isProtected: Rep[Boolean]

        def isPrivate: Rep[Boolean]

        def isStatic: Rep[Boolean]

        def isFinal: Rep[Boolean]
    }


    trait FieldInfoInfixOps
    {
        def declaringType: Rep[ReferenceType]

        def name: Rep[String]

        def fieldType: Rep[Type]

    }

    trait FieldDeclarationInfixOps
    {
        def fieldType: Rep[Type] 

        def value: Rep[Option[Any]] 

        def valueType: Rep[Option[FieldType]] 

        def isTransient: Rep[Boolean] 

        def isVolatile: Rep[Boolean] 

        def isEnum: Rep[Boolean] 

        def isSynthetic: Rep[Boolean] 
    }

    trait MethodInfoInfixOps
    {
        def receiverType: Rep[ReferenceType] 

        def name: Rep[String] 

        def returnType: Rep[Type] 

        def parameterTypes: Rep[Seq[FieldType]] 
    }

    trait MethodDeclarationInfixOps
    {

        def receiverType: Rep[ReferenceType] 

        def returnType: Rep[Type] 

        def parameterTypes: Rep[Seq[FieldType]] 

        def isSynchronized: Rep[Boolean] 

        def isBridge: Rep[Boolean] 

        def isVarArgs: Rep[Boolean] 

        def isNative: Rep[Boolean] 

        def isAbstract: Rep[Boolean] 

        def isStrict: Rep[Boolean] 

        def isSynthetic: Rep[Boolean] 

    }


    trait CodeAttributeInfixOps
    {
        def declaringMethod: Rep[MethodDeclaration] 

        def codeLength: Rep[Int] 

        def maxStack: Rep[Int] 

        def maxLocals: Rep[Int] 

        def exceptionHandlers: Rep[Seq[ExceptionHandler]] 
    }

    trait ExceptionHandlerInfixOps
    {
        def declaringMethod: Rep[MethodDeclaration] 

        def catchType: Rep[Option[Type]] 

        def startPC: Rep[Int] 

        def endPC: Rep[Int] 

        def handlerPC: Rep[Int] 
    }


    trait InnerClassAttributeInfixOps
    {
        def declaringClass: Rep[ClassDeclaration] 

        def innerClassType: Rep[Type] 

        def outerClassType: Rep[Option[Type]] 

        def innerName: Rep[Option[String]] 

        def innerClassAccessFlags: Rep[Int] 
    }


    trait EnclosingMethodAttributeInfixOps
    {
        def declaringClass: Rep[ClassDeclaration] 

        def name: Rep[Option[String]] 

        def parameterTypes: Rep[Option[Seq[FieldType]]] 

        def returnType: Rep[Option[Type]] 

        def outerClassType: Rep[ObjectType] 
    }


}