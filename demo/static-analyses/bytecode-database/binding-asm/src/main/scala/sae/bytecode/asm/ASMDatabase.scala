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

import sae.bytecode.BytecodeDatabase
import sae.bytecode.asm.reader.ASMProcessor
import sae.bytecode.asm.instructions.opcodes.{IINC, LOOKUPSWITCH, RET, TABLESWITCH}

/**
 *
 * @author Ralf Mitschke
 */
class ASMDatabase
    extends BytecodeDatabase
    with ASMTypes
    with ASMTypeOps
    with ASMTypeConstructors
    with ASMTypeOrdering
    with ASMStructure
    with ASMStructureRelations
    with ASMStructureOps
    with ASMStructureOrdering
    with ASMStructureDerived
    with ASMStructureDerivedConstructors
    with ASMStructureDerivedOps
    with ASMInstructions
    with ASMInstructionsRelations
    with ASMInstructionOps
    with ASMDatabaseManipulation
{

    def additionProcessor : ASMProcessor = AdditionProcessor

    def removalProcessor : ASMProcessor = RemovalProcessor

    object AdditionProcessor extends ASMProcessor
    {
        val database: ASMDatabase.this.type = ASMDatabase.this

        def processClassDeclaration (classDeclaration: ClassDeclaration) =
            database.classDeclarations += classDeclaration

        def processMethodDeclaration (methodDeclaration: MethodDeclaration) =
            database.methodDeclarations += methodDeclaration

        def processFieldDeclaration (fieldDeclaration: FieldDeclaration) =
            database.fieldDeclarations += fieldDeclaration

        def processCodeAttribute (codeAttribute: CodeAttribute) =
            database.codeAttributes += codeAttribute

        def processExceptionHandler (h: ExceptionHandler) =
            database.exceptionHandlers += h

        def processInnerClassAttribute (innerClassAttribute: InnerClassAttribute) =
            database.innerClassAttributes += innerClassAttribute

        def processEnclosingMethodAttribute (enclosingMethodAttribute: EnclosingMethodAttribute) =
            database.enclosingMethodAttributes += enclosingMethodAttribute

        def processBasicInstruction (i: Instruction) =
            database.basicInstructions += i

        def processFieldReadInstruction (i: FieldAccessInstruction) =
            database.fieldReadInstructions += i

        def processFieldWriteInstruction (i: FieldAccessInstruction) =
            database.fieldWriteInstructions += i

        def processUnconditionalJumpInstruction (i: JumpInstruction) =
            database.unconditionalJumpInstructions += i

        def processConditionalJumpInstruction (i: JumpInstruction) =
            database.conditionalJumpInstructions += i

        def processConstantValueInstruction[V] (i: ConstantValueInstruction[V]) =
            database.constantValueInstructions += i

        def processNewArrayInstruction[V] (i: NewArrayInstruction[V]) =
            database.newArrayInstructions += i

        def processLookupSwitchInstruction (i: LOOKUPSWITCH) =
            database.lookupSwitchInstructions += i

        def processTableSwitchInstruction (i: TABLESWITCH) =
            database.tableSwitchInstructions += i

        def processMethodInvocationInstruction (i: MethodInvocationInstruction) =
            database.methodInvocationInstructions += i

        def processObjectTypeInstruction (i: ObjectTypeInstruction) =
            database.objectTypeInstructions += i

        def processLocalVariableLoadInstructions (i: LocalVariableAccessInstruction) =
            database.localVariableLoadInstructions += i

        def processLocalVariableStoreInstructions (i: LocalVariableAccessInstruction) =
            database.localVariableStoreInstructions += i

        def processIINCInstruction (i: IINC) =
            database.integerIncrementInstructions += i

        def processRetInstructions (i: RET) =
            database.retInstructions += i
    }


    object RemovalProcessor extends ASMProcessor
    {
        val database: ASMDatabase.this.type = ASMDatabase.this

        def processClassDeclaration (classDeclaration: ClassDeclaration) =
            database.classDeclarations -= classDeclaration

        def processMethodDeclaration (methodDeclaration: MethodDeclaration) =
            database.methodDeclarations -= methodDeclaration

        def processFieldDeclaration (fieldDeclaration: FieldDeclaration) =
            database.fieldDeclarations -= fieldDeclaration

        def processCodeAttribute (codeAttribute: CodeAttribute) =
            database.codeAttributes -= codeAttribute

        def processExceptionHandler (h: ExceptionHandler) =
            database.exceptionHandlers -= h

        def processInnerClassAttribute (innerClassAttribute: InnerClassAttribute) =
            database.innerClassAttributes -= innerClassAttribute

        def processEnclosingMethodAttribute (enclosingMethodAttribute: EnclosingMethodAttribute) =
            database.enclosingMethodAttributes -= enclosingMethodAttribute

        def processBasicInstruction (i: Instruction) =
            database.basicInstructions -= i

        def processFieldReadInstruction (i: FieldAccessInstruction) =
            database.fieldReadInstructions -= i

        def processFieldWriteInstruction (i: FieldAccessInstruction) =
            database.fieldWriteInstructions -= i

        def processUnconditionalJumpInstruction (i: JumpInstruction) =
            database.unconditionalJumpInstructions -= i

        def processConditionalJumpInstruction (i: JumpInstruction) =
            database.conditionalJumpInstructions -= i

        def processConstantValueInstruction[V] (i: ConstantValueInstruction[V]) =
            database.constantValueInstructions -= i

        def processNewArrayInstruction[V] (i: NewArrayInstruction[V]) =
            database.newArrayInstructions -= i

        def processLookupSwitchInstruction (i: LOOKUPSWITCH) =
            database.lookupSwitchInstructions -= i

        def processTableSwitchInstruction (i: TABLESWITCH) =
            database.tableSwitchInstructions -= i

        def processMethodInvocationInstruction (i: MethodInvocationInstruction) =
            database.methodInvocationInstructions -= i

        def processObjectTypeInstruction (i: ObjectTypeInstruction) =
            database.objectTypeInstructions -= i

        def processLocalVariableLoadInstructions (i: LocalVariableAccessInstruction) =
            database.localVariableLoadInstructions -= i

        def processLocalVariableStoreInstructions (i: LocalVariableAccessInstruction) =
            database.localVariableStoreInstructions -= i

        def processIINCInstruction (i: IINC) =
            database.integerIncrementInstructions -= i

        def processRetInstructions (i: RET) =
            database.retInstructions -= i
    }

    protected def doEndTransaction () {
        classDeclarations.endTransaction ()
        methodDeclarations.endTransaction ()
        fieldDeclarations.endTransaction ()
        codeAttributes.endTransaction ()
        innerClassAttributes.endTransaction ()
        enclosingMethodAttributes.endTransaction ()
        basicInstructions.endTransaction ()
        fieldReadInstructions.endTransaction ()
        fieldWriteInstructions.endTransaction ()
        unconditionalJumpInstructions.endTransaction ()
        conditionalJumpInstructions.endTransaction ()
        constantValueInstructions.endTransaction ()
        newArrayInstructions.endTransaction ()
        lookupSwitchInstructions.endTransaction ()
        tableSwitchInstructions.endTransaction ()
        methodInvocationInstructions.endTransaction ()
        objectTypeInstructions.endTransaction ()
        localVariableLoadInstructions.endTransaction ()
        localVariableStoreInstructions.endTransaction ()
        integerIncrementInstructions.endTransaction ()
        retInstructions.endTransaction ()
    }
}
