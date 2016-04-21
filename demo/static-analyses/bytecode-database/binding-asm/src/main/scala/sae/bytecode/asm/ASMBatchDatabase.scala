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

import idb.Table
import sae.bytecode.BytecodeDatabase
import sae.bytecode.asm.instructions.opcodes.{RET, IINC, TABLESWITCH, LOOKUPSWITCH}
import sae.bytecode.asm.reader.ASMProcessor
import scala.collection.mutable

/**
 *
 * @author Ralf Mitschke
 */
class ASMBatchDatabase
    extends ASMDatabase
{
   override def additionProcessor = BatchAdditionProcessor

   override def removalProcessor = BatchRemovalProcessor

	val seqAddClassDeclarations : mutable.ListBuffer[ClassDeclaration] = new mutable.ListBuffer[ClassDeclaration]
	val seqAddMethodDeclarations : mutable.ListBuffer[MethodDeclaration] = new mutable.ListBuffer[MethodDeclaration]
	val seqAddFieldDeclarations : mutable.ListBuffer[FieldDeclaration] = new mutable.ListBuffer[FieldDeclaration]
	val seqAddCodeAttributes : mutable.ListBuffer[CodeAttribute] = new mutable.ListBuffer[CodeAttribute]
	val seqAddExceptionHandlers : mutable.ListBuffer[ExceptionHandler] = new mutable.ListBuffer[ExceptionHandler]
	val seqAddInnerClassAttributes : mutable.ListBuffer[InnerClassAttribute] = new mutable.ListBuffer[InnerClassAttribute]
	val seqAddEnclosingMethodAttributes : mutable.ListBuffer[EnclosingMethodAttribute] = new mutable.ListBuffer[EnclosingMethodAttribute]
	val seqAddBasicInstructions : mutable.ListBuffer[Instruction] = new mutable.ListBuffer[Instruction]
	val seqAddFieldReadInstructions : mutable.ListBuffer[FieldAccessInstruction] = new mutable.ListBuffer[FieldAccessInstruction]
	val seqAddFieldWriteInstructions : mutable.ListBuffer[FieldAccessInstruction] = new mutable.ListBuffer[FieldAccessInstruction]
	val seqAddUnconditionalJumpInstructions : mutable.ListBuffer[JumpInstruction] = new mutable.ListBuffer[JumpInstruction]
	val seqAddConditionalJumpInstructions : mutable.ListBuffer[JumpInstruction] = new mutable.ListBuffer[JumpInstruction]
	val seqAddConstantValueInstructions : mutable.ListBuffer[ConstantValueInstruction[_]] = new mutable.ListBuffer[ConstantValueInstruction[_]]
	val seqAddNewArrayInstructions : mutable.ListBuffer[NewArrayInstruction[_]] = new mutable.ListBuffer[NewArrayInstruction[_]]
	val seqAddLookupSwitchInstructions : mutable.ListBuffer[LOOKUPSWITCH] = new mutable.ListBuffer[LOOKUPSWITCH]
	val seqAddTableSwitchInstructions : mutable.ListBuffer[TABLESWITCH] = new mutable.ListBuffer[TABLESWITCH]
	val seqAddMethodInvocationInstructions : mutable.ListBuffer[MethodInvocationInstruction] = new mutable.ListBuffer[MethodInvocationInstruction]
	val seqAddObjectTypeInstructions : mutable.ListBuffer[ObjectTypeInstruction] = new mutable.ListBuffer[ObjectTypeInstruction]
	val seqAddLocalVariableLoadInstructions : mutable.ListBuffer[LocalVariableAccessInstruction] = new mutable.ListBuffer[LocalVariableAccessInstruction]
	val seqAddLocalVariableStoreInstructions : mutable.ListBuffer[LocalVariableAccessInstruction] = new mutable.ListBuffer[LocalVariableAccessInstruction]
	val seqAddIntegerIncrementInstructions : mutable.ListBuffer[IINC] = new mutable.ListBuffer[IINC]
	val seqAddRetInstructions : mutable.ListBuffer[RET] = new mutable.ListBuffer[RET]

	object BatchAdditionProcessor extends ASMProcessor
    {
        val database = ASMBatchDatabase.this

        def processClassDeclaration (classDeclaration: ClassDeclaration) =
            database.seqAddClassDeclarations += classDeclaration

        def processMethodDeclaration (methodDeclaration: MethodDeclaration) =
            database.seqAddMethodDeclarations += methodDeclaration

        def processFieldDeclaration (fieldDeclaration: FieldDeclaration) =
            database.seqAddFieldDeclarations += fieldDeclaration

        def processCodeAttribute (codeAttribute: CodeAttribute) =
            database.seqAddCodeAttributes += codeAttribute

        def processExceptionHandler (h: ExceptionHandler) =
            database.seqAddExceptionHandlers += h

        def processInnerClassAttribute (innerClassAttribute: InnerClassAttribute) =
            database.seqAddInnerClassAttributes += innerClassAttribute

        def processEnclosingMethodAttribute (enclosingMethodAttribute: EnclosingMethodAttribute) =
            database.seqAddEnclosingMethodAttributes += enclosingMethodAttribute

        def processBasicInstruction (i: Instruction) =
            database.seqAddBasicInstructions += i

        def processFieldReadInstruction (i: FieldAccessInstruction) =
            database.seqAddFieldReadInstructions += i

        def processFieldWriteInstruction (i: FieldAccessInstruction) =
            database.seqAddFieldWriteInstructions += i

        def processUnconditionalJumpInstruction (i: JumpInstruction) =
            database.seqAddUnconditionalJumpInstructions += i

        def processConditionalJumpInstruction (i: JumpInstruction) =
            database.seqAddConditionalJumpInstructions += i

        def processConstantValueInstruction[V] (i: ConstantValueInstruction[V]) =
            database.seqAddConstantValueInstructions += i

        def processNewArrayInstruction[V] (i: NewArrayInstruction[V]) =
            database.seqAddNewArrayInstructions += i

        def processLookupSwitchInstruction (i: LOOKUPSWITCH) =
            database.seqAddLookupSwitchInstructions += i

        def processTableSwitchInstruction (i: TABLESWITCH) =
            database.seqAddTableSwitchInstructions += i

        def processMethodInvocationInstruction (i: MethodInvocationInstruction) =
            database.seqAddMethodInvocationInstructions += i

        def processObjectTypeInstruction (i: ObjectTypeInstruction) =
            database.seqAddObjectTypeInstructions += i

        def processLocalVariableLoadInstructions (i: LocalVariableAccessInstruction) =
            database.seqAddLocalVariableLoadInstructions += i

        def processLocalVariableStoreInstructions (i: LocalVariableAccessInstruction) =
            database.seqAddLocalVariableStoreInstructions += i

        def processIINCInstruction (i: IINC) =
            database.seqAddIntegerIncrementInstructions += i

        def processRetInstructions (i: RET) =
            database.seqAddRetInstructions += i
    }


	val seqDelClassDeclarations : mutable.ListBuffer[ClassDeclaration] = new mutable.ListBuffer[ClassDeclaration]
	val seqDelMethodDeclarations : mutable.ListBuffer[MethodDeclaration] = new mutable.ListBuffer[MethodDeclaration]
	val seqDelFieldDeclarations : mutable.ListBuffer[FieldDeclaration] = new mutable.ListBuffer[FieldDeclaration]
	val seqDelCodeAttributes : mutable.ListBuffer[CodeAttribute] = new mutable.ListBuffer[CodeAttribute]
	val seqDelExceptionHandlers : mutable.ListBuffer[ExceptionHandler] = new mutable.ListBuffer[ExceptionHandler]
	val seqDelInnerClassAttributes : mutable.ListBuffer[InnerClassAttribute] = new mutable.ListBuffer[InnerClassAttribute]
	val seqDelEnclosingMethodAttributes : mutable.ListBuffer[EnclosingMethodAttribute] = new mutable.ListBuffer[EnclosingMethodAttribute]
	val seqDelBasicInstructions : mutable.ListBuffer[Instruction] = new mutable.ListBuffer[Instruction]
	val seqDelFieldReadInstructions : mutable.ListBuffer[FieldAccessInstruction] = new mutable.ListBuffer[FieldAccessInstruction]
	val seqDelFieldWriteInstructions : mutable.ListBuffer[FieldAccessInstruction] = new mutable.ListBuffer[FieldAccessInstruction]
	val seqDelUnconditionalJumpInstructions : mutable.ListBuffer[JumpInstruction] = new mutable.ListBuffer[JumpInstruction]
	val seqDelConditionalJumpInstructions : mutable.ListBuffer[JumpInstruction] = new mutable.ListBuffer[JumpInstruction]
	val seqDelConstantValueInstructions : mutable.ListBuffer[ConstantValueInstruction[_]] = new mutable.ListBuffer[ConstantValueInstruction[_]]
	val seqDelNewArrayInstructions : mutable.ListBuffer[NewArrayInstruction[_]] = new mutable.ListBuffer[NewArrayInstruction[_]]
	val seqDelLookupSwitchInstructions : mutable.ListBuffer[LOOKUPSWITCH] = new mutable.ListBuffer[LOOKUPSWITCH]
	val seqDelTableSwitchInstructions : mutable.ListBuffer[TABLESWITCH] = new mutable.ListBuffer[TABLESWITCH]
	val seqDelMethodInvocationInstructions : mutable.ListBuffer[MethodInvocationInstruction] = new mutable.ListBuffer[MethodInvocationInstruction]
	val seqDelObjectTypeInstructions : mutable.ListBuffer[ObjectTypeInstruction] = new mutable.ListBuffer[ObjectTypeInstruction]
	val seqDelLocalVariableLoadInstructions : mutable.ListBuffer[LocalVariableAccessInstruction] = new mutable.ListBuffer[LocalVariableAccessInstruction]
	val seqDelLocalVariableStoreInstructions : mutable.ListBuffer[LocalVariableAccessInstruction] = new mutable.ListBuffer[LocalVariableAccessInstruction]
	val seqDelIntegerIncrementInstructions : mutable.ListBuffer[IINC] = new mutable.ListBuffer[IINC]
	val seqDelRetInstructions : mutable.ListBuffer[RET] = new mutable.ListBuffer[RET]
	
	
    object BatchRemovalProcessor extends ASMProcessor
    {
		val database = ASMBatchDatabase.this

		def processClassDeclaration (classDeclaration: ClassDeclaration) =
			database.seqDelClassDeclarations += classDeclaration

		def processMethodDeclaration (methodDeclaration: MethodDeclaration) =
			database.seqDelMethodDeclarations += methodDeclaration

		def processFieldDeclaration (fieldDeclaration: FieldDeclaration) =
			database.seqDelFieldDeclarations += fieldDeclaration

		def processCodeAttribute (codeAttribute: CodeAttribute) =
			database.seqDelCodeAttributes += codeAttribute

		def processExceptionHandler (h: ExceptionHandler) =
			database.seqDelExceptionHandlers += h

		def processInnerClassAttribute (innerClassAttribute: InnerClassAttribute) =
			database.seqDelInnerClassAttributes += innerClassAttribute

		def processEnclosingMethodAttribute (enclosingMethodAttribute: EnclosingMethodAttribute) =
			database.seqDelEnclosingMethodAttributes += enclosingMethodAttribute

		def processBasicInstruction (i: Instruction) =
			database.seqDelBasicInstructions += i

		def processFieldReadInstruction (i: FieldAccessInstruction) =
			database.seqDelFieldReadInstructions += i

		def processFieldWriteInstruction (i: FieldAccessInstruction) =
			database.seqDelFieldWriteInstructions += i

		def processUnconditionalJumpInstruction (i: JumpInstruction) =
			database.seqDelUnconditionalJumpInstructions += i

		def processConditionalJumpInstruction (i: JumpInstruction) =
			database.seqDelConditionalJumpInstructions += i

		def processConstantValueInstruction[V] (i: ConstantValueInstruction[V]) =
			database.seqDelConstantValueInstructions += i

		def processNewArrayInstruction[V] (i: NewArrayInstruction[V]) =
			database.seqDelNewArrayInstructions += i

		def processLookupSwitchInstruction (i: LOOKUPSWITCH) =
			database.seqDelLookupSwitchInstructions += i

		def processTableSwitchInstruction (i: TABLESWITCH) =
			database.seqDelTableSwitchInstructions += i

		def processMethodInvocationInstruction (i: MethodInvocationInstruction) =
			database.seqDelMethodInvocationInstructions += i

		def processObjectTypeInstruction (i: ObjectTypeInstruction) =
			database.seqDelObjectTypeInstructions += i

		def processLocalVariableLoadInstructions (i: LocalVariableAccessInstruction) =
			database.seqDelLocalVariableLoadInstructions += i

		def processLocalVariableStoreInstructions (i: LocalVariableAccessInstruction) =
			database.seqDelLocalVariableStoreInstructions += i

		def processIINCInstruction (i: IINC) =
			database.seqDelIntegerIncrementInstructions += i

		def processRetInstructions (i: RET) =
			database.seqDelRetInstructions += i
    }

	private def doEndTransaction[A](table : Table[A], listAdd : mutable.ListBuffer[A], listDel : mutable.ListBuffer[A]) {
		table.addAll(listAdd.result())
		table.removeAll(listDel.result())
		listAdd.clear()
		listDel.clear()
		table.endTransaction()
	}

    override protected def doEndTransaction () {
    	doEndTransaction(classDeclarations, seqAddClassDeclarations, seqDelClassDeclarations)
		doEndTransaction(methodDeclarations, seqAddMethodDeclarations, seqDelMethodDeclarations)
		doEndTransaction(fieldDeclarations, seqAddFieldDeclarations, seqDelFieldDeclarations)
		doEndTransaction(codeAttributes, seqAddCodeAttributes, seqDelCodeAttributes)
		doEndTransaction(innerClassAttributes, seqAddInnerClassAttributes, seqDelInnerClassAttributes)
		doEndTransaction(enclosingMethodAttributes, seqAddEnclosingMethodAttributes, seqDelEnclosingMethodAttributes)
		doEndTransaction(basicInstructions, seqAddBasicInstructions, seqDelBasicInstructions)
		doEndTransaction(fieldReadInstructions, seqAddFieldReadInstructions, seqDelFieldReadInstructions)
		doEndTransaction(fieldWriteInstructions, seqAddFieldWriteInstructions, seqDelFieldWriteInstructions)
		doEndTransaction(unconditionalJumpInstructions, seqAddUnconditionalJumpInstructions, seqDelUnconditionalJumpInstructions)
		doEndTransaction(conditionalJumpInstructions, seqAddConditionalJumpInstructions, seqDelConditionalJumpInstructions)
		doEndTransaction(constantValueInstructions, seqAddConstantValueInstructions, seqDelConstantValueInstructions)
		doEndTransaction(newArrayInstructions, seqAddNewArrayInstructions, seqDelNewArrayInstructions)
		doEndTransaction(lookupSwitchInstructions, seqAddLookupSwitchInstructions, seqDelLookupSwitchInstructions)
		doEndTransaction(tableSwitchInstructions, seqAddTableSwitchInstructions, seqDelTableSwitchInstructions)
		doEndTransaction(methodInvocationInstructions, seqAddMethodInvocationInstructions, seqDelMethodInvocationInstructions)
		doEndTransaction(objectTypeInstructions, seqAddObjectTypeInstructions, seqDelObjectTypeInstructions)
		doEndTransaction(localVariableLoadInstructions, seqAddLocalVariableLoadInstructions, seqDelLocalVariableLoadInstructions)
		doEndTransaction(localVariableStoreInstructions, seqAddLocalVariableStoreInstructions, seqDelLocalVariableStoreInstructions)
		doEndTransaction(integerIncrementInstructions, seqAddIntegerIncrementInstructions, seqDelIntegerIncrementInstructions)
		doEndTransaction(retInstructions, seqAddRetInstructions, seqDelRetInstructions)
    }
}
