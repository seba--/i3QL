package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.constants.OpCodes

/**
 * @author Ralf Mitschke, Mirko Köhler
 */
object FI_USELESS extends (BytecodeDatabase => Relation[BytecodeDatabase#MethodDeclaration]) {

	def apply(database: BytecodeDatabase): Relation[BytecodeDatabase#MethodDeclaration] = {
		import database._

		val finalizeMethodInstructions : Relation[Instruction] =
			SELECT (*) FROM instructions WHERE (
				(i : Rep[Instruction]) =>
					i.declaringMethod.name == "finalize" AND
					i.declaringMethod.returnType == void AND
					i.declaringMethod.parameterTypes == Nil
			)

		val finalizeMethodInstructionsInvokeSpecial: Relation[MethodInvocationInstruction] =
			SELECT ((i : Rep[Instruction]) => i.AsInstanceOf[MethodInvocationInstruction]) FROM finalizeMethodInstructions WHERE
				(_.opcode == OpCodes.INVOKESPECIAL)

		val finalizeMethodSuperCalls: Relation[MethodInvocationInstruction] =
			SELECT (*) FROM finalizeMethodInstructionsInvokeSpecial WHERE (
				(i : Rep[MethodInvocationInstruction]) =>
					i.methodInfo.name == "finalize" AND
					i.methodInfo.returnType == void AND
					i.methodInfo.parameterTypes == Nil
			)

		val countInstructionsInFinalizers: Relation[(MethodDeclaration, Int)] =
			SELECT ((m : Rep[MethodDeclaration]) => m , COUNT (*)) FROM finalizeMethodInstructions GROUP BY ((i : Rep[Instruction]) => i.declaringMethod)

		val finalizersWithFiveInstructions: Relation[MethodDeclaration] =
			SELECT ((p : Rep[(MethodDeclaration, Int)]) => p._1 ) FROM countInstructionsInFinalizers WHERE ((p : Rep[(MethodDeclaration, Int)]) => p._2 == 5)

		SELECT (*) FROM finalizersWithFiveInstructions WHERE (
			(decl : Rep[MethodDeclaration]) => {
				EXISTS (
					SELECT (*) FROM finalizeMethodSuperCalls WHERE (
						(i : Rep[MethodInvocationInstruction]) => i.declaringMethod == decl
					)
				)
			}
		)

	}
}
