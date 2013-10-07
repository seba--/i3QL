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
import sae.bytecode.structure.instructions.{BytecodeInstructionsOps, BytecodeInstructionsManifest}
import sae.bytecode.structure.base.BytecodeStructureManifests
import sae.bytecode.types.BytecodeTypeManifests
import scala.virtualization.lms.common.StructExp

/**
 *
 * @author Ralf Mitschke
 */
trait ASMInstructionOps
    extends ASMInstructions
    with BytecodeInstructionsOps
    with BytecodeStructureManifests
    with BytecodeTypeManifests
    with BytecodeInstructionsManifest
{

    val IR: StructExp

    import IR._

    override implicit def instructionToInfixOps (i: Rep[Instruction]) =
        InstructionInfixOps (i)

    override implicit def constantValueInstructionToInfixOps[V: Manifest] (i: Rep[ConstantValueInstruction[V]]) =
        ConstantValueInstructionInfixOps[V](i)

    override implicit def fieldAccessInstructionToInfixOps (i: Rep[FieldAccessInstruction]) =
        FieldAccessInstructionInfixOps (i)

    override implicit def jumpInstructionToInfixOps (i: Rep[JumpInstruction]) =
        JumpInstructionInfixOps (i)

    override implicit def localVariableAccessInstructionToInfixOps (i: Rep[LocalVariableAccessInstruction]) =
        LocalVariableAccessInstructionInfixOps (i)

    override implicit def methodInvocationInstructionToInfixOps (i: Rep[MethodInvocationInstruction]) =
        MethodInvocationInstructionInfixOps (i)

    override implicit def newArrayInstructionToInfixOps[V: Manifest] (i: Rep[NewArrayInstruction[V]]) =
        NewArrayInstructionInfixOps[V](i)

    override implicit def objectTypeInstructionToInfixOps (i: Rep[ObjectTypeInstruction]) =
        ObjectTypeInstructionInfixOps (i)


    case class InstructionInfixOps (i: Rep[Instruction])
        extends super.InstructionInfixOps
    {
        def declaringMethod: Rep[MethodDeclaration] = field[MethodDeclaration](i, "declaringMethod")

        def pc: Rep[Int] = field[Int](i, "pc")

        def opcode: Rep[Short] = field[Short](i, "opcode")

        def nextPC: Rep[Int] = field[Int](i, "nextPC")

        def mnemonic: Rep[String] = field[String](i, "mnemonic")
    }


    case class ConstantValueInstructionInfixOps[V: Manifest] (i: Rep[ConstantValueInstruction[V]])
        extends super.ConstantValueInstructionInfixOps[V]
    {
        def value: Rep[V] = field[V](i, "value")
    }


    case class FieldAccessInstructionInfixOps (i: Rep[FieldAccessInstruction])
        extends super.FieldAccessInstructionInfixOps
    {
        def fieldInfo: Rep[FieldInfo] = field[FieldInfo](i, "fieldInfo")
    }


    case class JumpInstructionInfixOps (i: Rep[JumpInstruction])
        extends super.JumpInstructionInfixOps
    {
        def offset: Rep[Int] = field[Int](i, "offset")
    }


    case class LocalVariableAccessInstructionInfixOps (i: Rep[LocalVariableAccessInstruction])
        extends super.LocalVariableAccessInstructionInfixOps
    {
        def lvIndex: Rep[Int] = field[Int](i, "lvIndex")
    }


    case class MethodInvocationInstructionInfixOps (i: Rep[MethodInvocationInstruction])
        extends super.MethodInvocationInstructionInfixOps
    {
        def methodInfo: Rep[MethodInfo] = field[MethodInfo](i, "methodInfo")
    }


    case class NewArrayInstructionInfixOps[+V: Manifest] (i: Rep[NewArrayInstruction[V]])
        extends super.NewArrayInstructionInfixOps[V]
    {
        def elementType: Rep[V] = field[V](i, "elementType")

        def arrayType: Rep[ArrayType[V]] = field[ArrayType[V]](i, "arrayType")
    }


    case class ObjectTypeInstructionInfixOps (i: Rep[ObjectTypeInstruction])
        extends super.ObjectTypeInstructionInfixOps
    {
        def objectType: Rep[ObjectType] = field[ObjectType](i, "objectType")
    }

}