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
import sae.bytecode.types.BytecodeTypeOps
import scala.virtualization.lms.common.{FunctionsExp, StructExp, StaticDataExp}
import sae.bytecode.asm.util.ASMTypeUtils


/**
 *
 * @author Ralf Mitschke
 */
trait ASMTypeOps
    extends BytecodeTypeOps
    with ASMTypes
    with ASMTypeUtils
{
    override val IR: StructExp with StaticDataExp with FunctionsExp

    import IR._

    private val objectTypeName: Rep[ObjectType => String] = staticData (
        (t: ObjectType) => ObjectType_Name (t)
    )

    private val objectTypeClassName: Rep[ObjectType => String] = staticData (
        (t: ObjectType) => ObjectType_ClassName (t)
    )

    private val objectTypePackageName: Rep[ObjectType => String] = staticData (
        (t: ObjectType) => ObjectType_PackageName (t)
    )

	private val typeIsObjectType : Rep[Type => Boolean] = staticData (
		(t : Type) => t.getSort == org.objectweb.asm.Type.OBJECT
	)

	private val typeIsVoidType : Rep[Type => Boolean] = staticData (
		(t : Type) => t.getSort == org.objectweb.asm.Type.VOID
	)

	private val typeIsArrayType : Rep[Type => Boolean] = staticData (
		(t : Type) => t.getSort == org.objectweb.asm.Type.ARRAY
	)


	override implicit def typeToInfixOps (i: Rep[ObjectType]) =
		ASMTypeInfixOps (i)

	case class ASMTypeInfixOps (i: Rep[Type]) extends TypeInfixOps
	{
		def isObjectType : Rep[Boolean] = typeIsObjectType (i)

		def isVoidType : Rep[Boolean] = typeIsVoidType (i)

		def isArrayType : Rep[Boolean] = typeIsArrayType (i)
	}

    override implicit def objectTypeToInfixOps (i: Rep[ObjectType]) =
        ASMObjectTypeInfixOps (i)

    case class ASMObjectTypeInfixOps (i: Rep[ObjectType]) extends ObjectTypeInfixOps
    {
        def name: Rep[String] = objectTypeName (i)

        def packageName: Rep[String] = objectTypePackageName (i)

        def className: Rep[String] = objectTypeClassName (i)
    }

}
