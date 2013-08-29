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

import org.objectweb.asm.Type
import sae.bytecode.BytecodeTypeConstructors

/**
 *
 * @author Ralf Mitschke
 */
trait ASMTypeConstructors
    extends BytecodeTypeConstructors
    with ASMTypes
{
    def void: VoidType = Type.VOID_TYPE

    def char: PrimitiveType = Type.CHAR_TYPE

    def boolean: PrimitiveType = Type.BOOLEAN_TYPE

    def byte: PrimitiveType = Type.BYTE_TYPE

    def short: PrimitiveType = Type.SHORT_TYPE

    def int: PrimitiveType = Type.INT_TYPE

    def long: PrimitiveType = Type.LONG_TYPE

    def float: PrimitiveType = Type.FLOAT_TYPE

    def double: PrimitiveType = Type.DOUBLE_TYPE

    def ObjectType (desc: String): ObjectType =
        Type.getObjectType ("L" + desc + ";")

    def ArrayType[T <: Type] (componentType: T, dimensions: Int): ArrayType[T] =
        Type.getType ("[" * dimensions + componentType.getDescriptor)
}
