package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.constants.OpCodes

/**
 * @author Ralf Mitschke, Mirko Köhler
 */
object ITA_INEFFICIENT_TO_ARRAY
	extends (BytecodeDatabase => Relation[BytecodeDatabase#MethodInvocationInstruction]){


	def apply(database : BytecodeDatabase): Relation[BytecodeDatabase#MethodInvocationInstruction] = {
		import database._

		val iconst0 : Relation[Instruction] = SELECT (*) FROM instructions WHERE ((i : Rep[Instruction]) => i.opcode == OpCodes.ICONST_0 )

		val anewarray : Relation[Instruction] = SELECT (*) FROM instructions WHERE ((i : Rep[Instruction]) => i.opcode == OpCodes.ANEWARRAY )

		val newArray0 : Relation[Instruction] =
			SELECT ((i: Rep[Instruction], a: Rep[Instruction]) => a) FROM (iconst0, anewarray) WHERE
				((i : Rep[Instruction], a : Rep[Instruction]) => i.nextPC == a.pc)

		val invokes: Relation[MethodInvocationInstruction] =
				SELECT ((i: Rep[MethodInvocationInstruction], a: Rep[Instruction]) => i) FROM (methodInvocationInstructions, newArray0) WHERE (
					(invoke : Rep[MethodInvocationInstruction], a : Rep[Instruction])  =>
					((invoke.opcode == OpCodes.INVOKEINTERFACE) OR (invoke.opcode == OpCodes.INVOKEVIRTUAL)) AND
					(invoke.methodInfo.name == "toArray") AND
					(invoke.methodInfo.returnType ==  ArrayType (ObjectType ("java/lang/Object"))) AND
					(invoke.methodInfo.parameterTypes == Seq ( ArrayType (ObjectType ("java/lang/Object")))) AND
					(invoke.pc == a.nextPC)
				)

		SELECT (*) FROM (invokes) WHERE ((invoke : Rep[MethodInvocationInstruction]) =>
			EXISTS (
				SELECT (*) FROM subTyping WHERE ((typing : Rep[TypeRelation]) =>
					(typing.superType == ObjectType ("java/util/Collection")) AND
					(typing.subType == invoke.methodInfo.receiverType)
				)
			)
		)

	}
}





/*object ITA_INEFFICIENT_TO_ARRAY
	extends (BytecodeDatabase => Relation[InvokeInstruction])
{


	val objectArrayType = ArrayType (ClassType ("java/lang/Object"))

	val collectionInterface = ClassType ("java/util/Collection")

	val listInterface = ClassType ("java/util/List")

	def nextSequenceIndex: InstructionInfo => Int = _.sequenceIndex + 1
	def previousSequenceIndex: InstructionInfo => Int = _.sequenceIndex - 1

	def instructionIndex : InstructionInfo => (MethodDeclaration, Int) = instr => (instr.declaringMethod, instr.sequenceIndex)

	def nextInstructionIndex : InstructionInfo => (MethodDeclaration, Int) = instr => (instr.declaringMethod, instr.sequenceIndex + 1)

	def prevInstructionIndex : InstructionInfo => (MethodDeclaration, Int) = instr => (instr.declaringMethod, instr.sequenceIndex - 1)

	def apply(database: BytecodeDatabase): Relation[InvokeInstruction] = {
		import database._

		val iconst0: Relation[ICONST_0] = SELECT ((_: InstructionInfo).asInstanceOf[ICONST_0]) FROM instructions WHERE (_.isInstanceOf[ICONST_0])

		val anewarray: Relation[ANEWARRAY] = SELECT ((_: InstructionInfo).asInstanceOf[ANEWARRAY]) FROM instructions WHERE (_.isInstanceOf[ANEWARRAY])

		val newArray0 =
			SELECT ((i: ICONST_0, a: ANEWARRAY) => a) FROM (iconst0, anewarray) WHERE
				(nextInstructionIndex === instructionIndex)

		val invokes: Relation[InvokeInstruction] =
			SELECT ((i: InvokeInstruction, a: ANEWARRAY) => i) FROM (invokeInterface.asInstanceOf[Relation[InvokeInstruction]], newArray0) WHERE
				(_.name == "toArray") AND
				(_.returnType == objectArrayType) AND
				(_.parameterTypes == Seq (objectArrayType)) AND
				(prevInstructionIndex === instructionIndex) UNION_ALL (
				SELECT ((i: InvokeInstruction, a: ANEWARRAY) => i) FROM (invokeVirtual.asInstanceOf[Relation[InvokeInstruction]], newArray0) WHERE
					(_.name == "toArray") AND
					(_.returnType == objectArrayType) AND
					(_.parameterTypes == Seq (objectArrayType)) AND
					(prevInstructionIndex === instructionIndex)
				)

		SELECT (*) FROM (invokes) WHERE EXISTS (
			SELECT (*) FROM subTypes WHERE
				(_.superType == collectionInterface) AND
				(subType === receiverType)
		)
	}  */