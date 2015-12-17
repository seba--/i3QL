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

import idb.SetTable
import idb.query.QueryEnvironment
import sae.bytecode.structure.instructions.{BytecodeInstructionsManifest, BytecodeInstructionsRelations}
import idb.syntax.iql._
import sae.bytecode.asm.instructions.opcodes.{TABLESWITCH, LOOKUPSWITCH, RET, IINC}

/**
 *
 * @author Ralf Mitschke
 */
trait ASMInstructionsRelations
    extends ASMInstructions
    with BytecodeInstructionsManifest
    with BytecodeInstructionsRelations
{
	private implicit val queryEnvironment = QueryEnvironment.Default

    val basicInstructions =
        SetTable.empty[Instruction]

    val fieldReadInstructions =
        SetTable.empty[FieldAccessInstruction]

    val fieldWriteInstructions =
        SetTable.empty[FieldAccessInstruction]

    val constantValueInstructions =
        SetTable.empty[ConstantValueInstruction[Any]]

    val lookupSwitchInstructions =
        SetTable.empty[LOOKUPSWITCH]

    val tableSwitchInstructions =
        SetTable.empty[TABLESWITCH]

    val methodInvocationInstructions =
        SetTable.empty[MethodInvocationInstruction]

    val objectTypeInstructions =
        SetTable.empty[ObjectTypeInstruction]

    val newArrayInstructions =
        SetTable.empty[NewArrayInstruction[Any]]

    val localVariableLoadInstructions =
        SetTable.empty[LocalVariableAccessInstruction]

    val localVariableStoreInstructions =
        SetTable.empty[LocalVariableAccessInstruction]

    val retInstructions =
        SetTable.empty[RET]

    val integerIncrementInstructions =
        SetTable.empty[IINC]

    val conditionalJumpInstructions =
        SetTable.empty[JumpInstruction]

    val unconditionalJumpInstructions =
        SetTable.empty[JumpInstruction]


    lazy val jumpInstructions = compile (
        conditionalJumpInstructions
            UNION ALL (unconditionalJumpInstructions)
    )


    lazy val fieldAccessInstructions = compile (
        fieldReadInstructions
            UNION ALL (fieldWriteInstructions)
    )


    lazy val localVariableAccessInstructions = compile (
        localVariableLoadInstructions
            UNION ALL (localVariableStoreInstructions)
            UNION ALL (integerIncrementInstructions)
            UNION ALL (retInstructions)
    )


    lazy val instructions = compile (
        basicInstructions
            UNION ALL (fieldAccessInstructions)
            UNION ALL (constantValueInstructions)
            UNION ALL (lookupSwitchInstructions)
            UNION ALL (tableSwitchInstructions)
            UNION ALL (methodInvocationInstructions)
            UNION ALL (objectTypeInstructions)
            UNION ALL (newArrayInstructions)
            UNION ALL (localVariableAccessInstructions)
            UNION ALL (jumpInstructions)
    )

}