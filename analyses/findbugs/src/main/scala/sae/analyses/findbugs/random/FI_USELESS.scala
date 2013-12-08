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

	/*
	def apply(database: BytecodeDatabase): Relation[MethodDeclaration] = {
		import database._


		val finalizeMethodInstructions =
			compile (
				SELECT (*) FROM instructions WHERE
					(_.declaringMethod.name == "finalize") AND
					(_.declaringMethod.returnType == void) AND
					(_.declaringMethod.parameterTypes == Nil)
			)

		val finalizeMethodInstructionsInvokeSpecial: Relation[INVOKESPECIAL] =
			SELECT ((_: InstructionInfo).asInstanceOf[INVOKESPECIAL]) FROM finalizeMethodInstructions WHERE
				(_.isInstanceOf[INVOKESPECIAL])

		val finalizeMethodSuperCalls: Relation[INVOKESPECIAL] =
			SELECT (*) FROM finalizeMethodInstructionsInvokeSpecial WHERE
				(_.name == "finalize") AND
				(_.returnType == void) AND
				(_.parameterTypes == Nil)

		import sae.syntax.RelationalAlgebraSyntax._

		val countInstructionsInFinalizers: Relation[(MethodDeclaration, Int)] = γ (
			finalizeMethodInstructions,
			declaringMethod,
			Count[InstructionInfo](),
			(m: MethodDeclaration, count: Int) => (m, count)
		)

		val finalizersWithFiveInstructions: Relation[MethodDeclaration] =
			compile (
				SELECT ((_: (MethodDeclaration, Int))._1) FROM countInstructionsInFinalizers WHERE (_._2 == 5)
			)

		SELECT (*) FROM finalizersWithFiveInstructions WHERE
			EXISTS (
				SELECT (*) FROM finalizeMethodSuperCalls WHERE
					(declaringMethod === identity[MethodDeclaration] _)
			)
	}


}      */
