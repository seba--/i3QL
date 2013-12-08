package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.asm.instructions.opcodes.INVOKESPECIAL
import sae.bytecode.constants.OpCodes



/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 */
object BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION extends (BytecodeDatabase => Relation[BytecodeDatabase#MethodInvocationInstruction]) {

	def apply(database : BytecodeDatabase) : Relation[BytecodeDatabase#MethodInvocationInstruction] = {
		import database._
		SELECT (
			(a : Rep[MethodInvocationInstruction], b : Rep[MethodInvocationInstruction]) => b
		) FROM (
			methodInvocationInstructions, methodInvocationInstructions
		) WHERE (
			(invSpecial : Rep[MethodInvocationInstruction], invVirtual : Rep[MethodInvocationInstruction]) =>
				invSpecial.opcode == OpCodes.INVOKESPECIAL AND
				invVirtual.opcode == OpCodes.INVOKEVIRTUAL AND
				invSpecial.declaringMethod == invVirtual.declaringMethod AND
				invSpecial.methodInfo.receiverType == invVirtual.methodInfo.receiverType AND
				invSpecial.nextPC == invVirtual.pc AND
				invSpecial.methodInfo.parameterTypes.length == 1 AND
				NOT (invSpecial.methodInfo.parameterTypes(0) == invVirtual.methodInfo.returnType) AND
				NOT (invSpecial.methodInfo.parameterTypes(0).IsInstanceOf[ReferenceType]) AND
				invSpecial.declaringMethod.declaringClass.majorVersion >= 49 AND
				invSpecial.methodInfo.receiverType.IsInstanceOf[ObjectType] AND
				invSpecial.methodInfo.receiverType.AsInstanceOf[ObjectType].className.startsWith ("java/lang") AND
				invVirtual.methodInfo.parameterTypes == Nil AND
				invVirtual.methodInfo.name.endsWith ("Value")
		)
	}

	/*SELECT ((a: INVOKESPECIAL, b: INVOKEVIRTUAL) => b) FROM
		(invokeSpecial, invokeVirtual) WHERE
		(declaringMethod === declaringMethod) AND
		(receiverType === receiverType) AND
		(sequenceIndex === ((second: INVOKEVIRTUAL) => second.sequenceIndex - 1)) AND
		NOT (firstParamType === returnType) AND
		(_.parameterTypes.size == 1) AND
		NOT ((_:INVOKESPECIAL).parameterTypes(0).isReferenceType) AND
		(_.declaringMethod.declaringClass.majorVersion >= 49) AND
		(_.receiverType.isObjectType) AND
		(_.receiverType.asInstanceOf[ClassType].className.startsWith ("java/lang")) AND
		((_: INVOKEVIRTUAL).parameterTypes == Nil) AND
		(_.name.endsWith ("Value"))  */
}
