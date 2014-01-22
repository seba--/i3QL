package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.constants.OpCodes



/**
 * @author Ralf Mitschke, Mirko Köhler
 */
object DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT extends (BytecodeDatabase => Relation[BytecodeDatabase#MethodInvocationInstruction]) {

	def apply(database : BytecodeDatabase) : Relation[BytecodeDatabase#MethodInvocationInstruction] = {
		import database._
		SELECT (
			(a : Rep[Instruction], b : Rep[MethodInvocationInstruction]) => b
		) FROM (
			instructions, methodInvocationInstructions
		) WHERE (
			(i2l : Rep[Instruction], invStatic : Rep[MethodInvocationInstruction]) => {
				i2l.opcode == OpCodes.I2L AND
				invStatic.opcode == OpCodes.INVOKESTATIC AND
				i2l.declaringMethod == invStatic.declaringMethod AND
				i2l.nextPC == invStatic.pc AND
				invStatic.methodInfo.receiverType == ObjectType("java/lang/Double") AND
				invStatic.methodInfo.name == "longBitsToDouble" AND
				invStatic.methodInfo.returnType == double AND
				invStatic.methodInfo.parameterTypes == Seq(long)
			}
		)
	}
	/*def apply(database: BytecodeDatabase): Relation[INVOKESTATIC] = {
		import database._

		val intToLong : Relation[I2L] = SELECT ((_: InstructionInfo).asInstanceOf[I2L]) FROM instructions WHERE (_.isInstanceOf[I2L])

		SELECT ((a: I2L, b: INVOKESTATIC) => b) FROM
			(intToLong, invokeStatic) WHERE
			(declaringMethod === declaringMethod) AND
			(sequenceIndex === ((second: INVOKESTATIC) => second.sequenceIndex - 1)) AND
			((_: INVOKESTATIC).receiverType == doubleClass) AND
			(_.name == "longBitsToDouble") AND
			(_.returnType == DoubleType) AND
			(_.parameterTypes == List(LongType))
	}      */
}
