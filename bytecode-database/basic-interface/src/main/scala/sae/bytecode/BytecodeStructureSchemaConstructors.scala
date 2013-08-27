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
import scala.reflect.SourceContext

/**
 *
 * @author Ralf Mitschke
 */
trait BytecodeStructureSchemaConstructors
    extends BytecodeStructure
    with BytecodeTypesSchema
{

    val IR: StructExp

    import IR.Rep
    import IR.struct
    import IR.ClassTag

    def ClassDeclaration (
        minorVersion: Rep[Int],
        majorVersion: Rep[Int],
        accessFlags: Rep[Int],
        classType: Rep[ObjectType],
        superClass: Rep[Option[ObjectType]],
        interfaces: Rep[Seq[ObjectType]]
    )(implicit ctx: SourceContext) =
        struct[ClassDeclaration](
            ClassTag[ClassDeclaration]("ClassDeclaration"),
            Map ("minorVersion" -> minorVersion,
                "majorVersion" -> majorVersion,
                "accessFlags" -> accessFlags,
                "classType" -> classType,
                "superClass" -> superClass,
                "interfaces" -> interfaces)
        )

    def MethodDeclaration (
        declaringClass: Rep[ClassDeclaration],
        accessFlags: Rep[Int],
        name: Rep[String],
        returnType: Rep[Type],
        parameterTypes: Rep[Seq[FieldType]]
    )(implicit ctx: SourceContext) =
        struct[MethodDeclaration](
            ClassTag[MethodDeclaration]("MethodDeclaration"),
            Map ("declaringClass" -> declaringClass,
                "accessFlags" -> accessFlags,
                "name" -> name,
                "returnType" -> returnType,
                "parameterTypes" -> parameterTypes)
        )

}