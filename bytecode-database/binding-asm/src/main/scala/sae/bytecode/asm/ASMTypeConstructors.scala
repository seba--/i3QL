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

import scala.virtualization.lms.common.{FunctionsExp, StaticDataExp}
import org.objectweb.asm.Type
import sae.bytecode.types.BytecodeTypeConstructors
import sae.bytecode.asm.util.ASMTypeUtils

/**
 *
 * @author Ralf Mitschke
 */
trait ASMTypeConstructors
    extends BytecodeTypeConstructors
    with ASMTypes
    with ASMTypeUtils
{
    override val IR: StaticDataExp with FunctionsExp

    import IR._

    override def void: Rep[VoidType] = staticData (Type.VOID_TYPE)

    override def char: Rep[PrimitiveType] = staticData (Type.CHAR_TYPE)

    override def boolean: Rep[PrimitiveType] = staticData (Type.BOOLEAN_TYPE)

    override def byte: Rep[PrimitiveType] = staticData (Type.BYTE_TYPE)

    override def short: Rep[PrimitiveType] = staticData (Type.SHORT_TYPE)

    override def int: Rep[PrimitiveType] = staticData (Type.INT_TYPE)

    override def long: Rep[PrimitiveType] = staticData (Type.LONG_TYPE)

    override def float: Rep[PrimitiveType] = staticData (Type.FLOAT_TYPE)

    override def double: Rep[PrimitiveType] = staticData (Type.DOUBLE_TYPE)


    private val objectType: Rep[String => ObjectType] = staticData (
        (s: String) => Type.getObjectType (s)
    )

    override def ObjectType (desc: Rep[String]): Rep[ObjectType] = objectType (desc)

	private def arrayType[T <: Type : Manifest] : Rep[T => ArrayType[T]] = staticData (
		(t : Type) => Type.getType("[" + t.getDescriptor).asInstanceOf[ArrayType[T]]
	)

	override def ArrayType[T  <: Type: Manifest] (componentType: Rep[T]): Rep[ArrayType[T]] = arrayType[T].apply(componentType)

    /*
    def ArrayType[T <: Type] (componentType: Rep[T], dimensions: Rep[Int]): Rep[ArrayType[T]]
    */
    /*
    case class ASMTypeInfixOps(t:Rep[Type]) {

        def getObjectType(desc: Rep[String]) =

    }
    */
}
