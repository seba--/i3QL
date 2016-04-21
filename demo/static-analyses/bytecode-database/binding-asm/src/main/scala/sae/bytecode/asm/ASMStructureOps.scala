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

import scala.language.implicitConversions
import sae.bytecode.structure.base.{BytecodeStructureOps, BytecodeStructureManifests}
import sae.bytecode.types.BytecodeTypeManifests
import scala.virtualization.lms.common.StructExp

/**
 *
 * @author Ralf Mitschke
 */
trait ASMStructureOps
    extends ASMStructure
    with BytecodeStructureOps
    with BytecodeStructureManifests
    with BytecodeTypeManifests
{

    val IR: StructExp

    import IR._


    override implicit def classDeclarationToInfixOps (c: Rep[ClassDeclaration]) =
        ClassDeclarationInfixOps (c)

    override implicit def fieldInfoToInfixOps (f: Rep[FieldInfo]) =
        FieldInfoInfixOps (f)

    override implicit def fieldDeclarationToInfixOps (f: Rep[FieldDeclaration]) =
        FieldDeclarationInfixOps (f)

    override implicit def fieldDeclarationToDeclaredClassMemberInfixOps (f: Rep[FieldDeclaration]) =
        DeclaredClassMemberInfixOps (f)

    override implicit def methodDeclarationToDeclaredClassMemberInfixOps (m: Rep[MethodDeclaration]) =
        DeclaredClassMemberInfixOps (m)

    override implicit def methodDeclarationToInfixOps (m: Rep[MethodDeclaration]) =
        new MethodDeclarationInfixOps (m)

    override implicit def methodInfoToInfixOps (m: Rep[MethodInfo]) =
        MethodInfoInfixOps (m)

    override implicit def codeAttributeToInfixOps (c: Rep[CodeAttribute]) =
        CodeAttributeInfixOps (c)

    override implicit def exceptionHandlerToInfixOps (e: Rep[ExceptionHandler]) =
        ExceptionHandlerInfixOps (e)

    override implicit def innerClassAttributeToInfixOps (i: Rep[InnerClassAttribute]) =
        InnerClassAttributeInfixOps (i)

    override implicit def enclosingMethodAttributeToInfixOps (e: Rep[EnclosingMethodAttribute]) =
        EnclosingMethodAttributeInfixOps (e)

    case class ClassDeclarationInfixOps (c: Rep[ClassDeclaration])
        extends super.ClassDeclarationInfixOps
    {

        def minorVersion: Rep[Int] = field[Int](c, "minorVersion")

        def majorVersion: Rep[Int] = field[Int](c, "majorVersion")

        def accessFlags: Rep[Int] = field[Int](c, "accessFlags")

        def classType: Rep[ObjectType] = field[ObjectType](c, "classType")

        def superType: Rep[Option[ObjectType]] = field[Option[ObjectType]](c, "superType")

        def interfaces: Rep[Seq[ObjectType]] = field[Seq[ObjectType]](c, "interfaces")

        // Note that the following are rather derived values and not technically fields
        // But the code generation will just make them o.fieldName calls, which are valid
        def isAnnotation: Rep[Boolean] = field[Boolean](c, "isAnnotation")

        def isClass: Rep[Boolean] = field[Boolean](c, "isClass")

        def isEnum: Rep[Boolean] = field[Boolean](c, "isEnum")

        def isInterface: Rep[Boolean] = field[Boolean](c, "isInterface")

        def isPublic: Rep[Boolean] = field[Boolean](c, "isPublic")

        def isDefault: Rep[Boolean] = field[Boolean](c, "isDefault")

        def isFinal: Rep[Boolean] = field[Boolean](c, "isFinal")

        def isAbstract: Rep[Boolean] = field[Boolean](c, "isAbstract")

        def isSynthetic: Rep[Boolean] = field[Boolean](c, "isSynthetic")

    }

    case class DeclaredClassMemberInfixOps (m: Rep[DeclaredClassMember])
        extends super.DeclaredClassMemberInfixOps
    {

        def declaringClass: Rep[ClassDeclaration] = field[ClassDeclaration](m, "declaringClass")

        def declaringType: Rep[ObjectType] = field[ObjectType](m, "declaringType")

        def accessFlags: Rep[Int] = field[Int](m, "accessFlags")

        def name: Rep[String] = field[String](m, "name")

        def isPublic: Rep[Boolean] = field[Boolean](m, "isPublic")

        def isProtected: Rep[Boolean] = field[Boolean](m, "isProtected")

        def isPrivate: Rep[Boolean] = field[Boolean](m, "isPrivate")

        def isStatic: Rep[Boolean] = field[Boolean](m, "isStatic")

        def isFinal: Rep[Boolean] = field[Boolean](m, "isFinal")
    }


    case class FieldInfoInfixOps (f: Rep[FieldInfo])
        extends super.FieldInfoInfixOps
    {
        def declaringType: Rep[ReferenceType] = field[ReferenceType](f, "declaringType")

        def name: Rep[String] = field[String](f, "name")

        def fieldType: Rep[Type] = field[Type](f, "fieldType")
    }

    case class FieldDeclarationInfixOps (f: Rep[FieldDeclaration])
    extends super.FieldDeclarationInfixOps
    {
        def fieldType: Rep[Type] = field[Type](f, "fieldType")

        def value: Rep[Option[Any]] = field[Option[Any]](f, "value")

        def valueType: Rep[Option[FieldType]] = field[Option[FieldType]](f, "valueType")

        def isTransient: Rep[Boolean] = field[Boolean](f, "isTransient")

        def isVolatile: Rep[Boolean] = field[Boolean](f, "isVolatile")

        def isEnum: Rep[Boolean] = field[Boolean](f, "isEnum")

        def isSynthetic: Rep[Boolean] = field[Boolean](f, "isSynthetic")
    }

    case class MethodInfoInfixOps (m: Rep[MethodInfo])
        extends super.MethodInfoInfixOps
    {
        def receiverType: Rep[ReferenceType] = field[ReferenceType](m, "receiverType")

        def name: Rep[String] = field[String](m, "name")

        def returnType: Rep[Type] = field[Type](m, "returnType")

        def parameterTypes: Rep[Seq[FieldType]] = field[Seq[FieldType]](m, "parameterTypes")
    }

    case class MethodDeclarationInfixOps (m: Rep[MethodDeclaration])
        extends super.MethodDeclarationInfixOps
    {

        def receiverType: Rep[ReferenceType] = field[ReferenceType](m, "receiverType")

        def returnType: Rep[Type] = field[Type](m, "returnType")

        def parameterTypes: Rep[Seq[FieldType]] = field[Seq[FieldType]](m, "parameterTypes")

        def isSynchronized: Rep[Boolean] = field[Boolean](m, "isSynchronized")

        def isBridge: Rep[Boolean] = field[Boolean](m, "isBridge")

        def isVarArgs: Rep[Boolean] = field[Boolean](m, "isVarArgs")

        def isNative: Rep[Boolean] = field[Boolean](m, "isNative")

        def isAbstract: Rep[Boolean] = field[Boolean](m, "isAbstract")

        def isStrict: Rep[Boolean] = field[Boolean](m, "isStrict")

        def isSynthetic: Rep[Boolean] = field[Boolean](m, "isSynthetic")

    }


    case class CodeAttributeInfixOps (c: Rep[CodeAttribute])
        extends super.CodeAttributeInfixOps
    {
        def declaringMethod: Rep[MethodDeclaration] = field[MethodDeclaration](c, "declaringMethod")

        def codeLength: Rep[Int] = field[Int](c, "codeLength")

        def maxStack: Rep[Int] = field[Int](c, "maxStack")

        def maxLocals: Rep[Int] = field[Int](c, "maxLocals")

        def exceptionHandlers: Rep[Seq[ExceptionHandler]] = field[Seq[ExceptionHandler]](c, "exceptionHandlers")
    }

    case class ExceptionHandlerInfixOps (e: Rep[ExceptionHandler])
        extends super.ExceptionHandlerInfixOps
    {
        def declaringMethod: Rep[MethodDeclaration] = field[MethodDeclaration](e, "declaringMethod")

        def catchType: Rep[Option[Type]] = field[Option[Type]](e, "catchType")

        def startPC: Rep[Int] = field[Int](e, "startPC")

        def endPC: Rep[Int] = field[Int](e, "endPC")

        def handlerPC: Rep[Int] = field[Int](e, "handlerPC")
    }


    case class InnerClassAttributeInfixOps (i: Rep[InnerClassAttribute])
        extends super.InnerClassAttributeInfixOps
    {
        def declaringClass: Rep[ClassDeclaration] = field[ClassDeclaration](i, "declaringClass")

        def innerClassType: Rep[Type] = field[Type](i, "innerClassType")

        def outerClassType: Rep[Option[Type]] = field[Option[Type]](i, "outerClassType")

        def innerName: Rep[Option[String]] = field[Option[String]](i, "innerName")

        def innerClassAccessFlags: Rep[Int] = field[Int](i, "innerClassAccessFlags")
    }


    case class EnclosingMethodAttributeInfixOps (e: Rep[EnclosingMethodAttribute])
        extends super.EnclosingMethodAttributeInfixOps
    {
        def declaringClass: Rep[ClassDeclaration] = field[ClassDeclaration](e, "declaringClass")

        def name: Rep[Option[String]] = field[Option[String]](e, "name")

        def parameterTypes: Rep[Option[Seq[FieldType]]] = field[Option[Seq[FieldType]]](e, "parameterTypes")

        def returnType: Rep[Option[Type]] = field[Option[Type]](e, "returnType")

        def outerClassType: Rep[ObjectType] = field[ObjectType](e, "outerClassType")
    }

}
