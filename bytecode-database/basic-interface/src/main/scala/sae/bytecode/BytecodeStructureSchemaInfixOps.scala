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

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

/**
 *
 * @author Ralf Mitschke
 */
trait BytecodeStructureSchemaInfixOps
    extends BytecodeStructure
    with BytecodeTypesSchema
{

    val IR: StructExp

    import IR._

    implicit def classDeclarationToInfixOps (c: Rep[ClassDeclaration]) =
        ClassDeclarationInfixOps (c)

    implicit def declaredClassMemberToInfixOps (m: Rep[DeclaredClassMember]) =
        DeclaredClassMemberInfixOps (m)

    implicit def methodDeclarationToDeclaredClassMemberInfixOps (m: Rep[MethodDeclaration]) =
        DeclaredClassMemberInfixOps (m)

    implicit def methodDeclarationToInfixOps (m: Rep[MethodDeclaration]) =
        MethodDeclarationInfixOps (m)

    case class ClassDeclarationInfixOps (c: Rep[ClassDeclaration])
    {

        def minorVersion: Rep[Int] = field[Int](c, "minorVersion")

        def majorVersion: Rep[Int] = field[Int](c, "majorVersion")

        def accessFlags: Rep[Int] = field[Int](c, "accessFlags")

        def classType: Rep[ObjectType] = field[ObjectType](c, "classType")

        def superClass: Rep[Option[ObjectType]] = field[Option[ObjectType]](c, "superClass")

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


    case class MethodDeclarationInfixOps (m: Rep[MethodDeclaration])
    {

        def returnType: Rep[Type] = field[Type](m, "returnType")

        def parameterTypes: Rep[Seq[FieldType]] = field[Seq[FieldType]](m, "parameterTypes")

        def receiverType: Rep[ReferenceType] = field[ReferenceType](m, "receiverType")

        def isSynchronized: Rep[Boolean] = field[Boolean](m, "isSynchronized")

        def isBridge: Rep[Boolean] = field[Boolean](m, "isBridge")

        def isVarArgs: Rep[Boolean] = field[Boolean](m, "isVarArgs")

        def isNative: Rep[Boolean] = field[Boolean](m, "isNative")

        def isAbstract: Rep[Boolean] = field[Boolean](m, "isAbstract")

        def isStrict: Rep[Boolean] = field[Boolean](m, "isStrict")

        def isSynthetic: Rep[Boolean] = field[Boolean](m, "isSynthetic")

    }


}