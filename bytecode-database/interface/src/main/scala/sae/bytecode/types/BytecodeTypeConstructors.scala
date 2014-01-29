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
package sae.bytecode.types

import scala.virtualization.lms.common.Base

/**
 *
 * @author Ralf Mitschke
 */
trait BytecodeTypeConstructors
    extends BytecodeTypes
{
    val IR: Base

    import IR._

    def void: Rep[VoidType]

    def char: Rep[PrimitiveType]

    def boolean: Rep[PrimitiveType]

    def byte: Rep[PrimitiveType]

    def short: Rep[PrimitiveType]

    def int: Rep[PrimitiveType]

    def long: Rep[PrimitiveType]

    def float: Rep[PrimitiveType]

    def double: Rep[PrimitiveType]

    /**
     * Constructs a new object type, i.e., a type describing a class.
     * @param desc A fully qualified class name in plain Java notation, e.g., "java.lang.String"
     */
    def ObjectType (desc: Rep[String]): Rep[ObjectType]

	/**
	 * Constructs a new array type.
	 * @param componentType The type of elements.
	 */
	def ArrayType[T <: Type : Manifest] (componentType: Rep[T]): Rep[ArrayType[T]]

    /*
    def ArrayType[T <: Type] (componentType: Rep[T], dimensions: Rep[Int]): Rep[ArrayType[T]]
     */
}
