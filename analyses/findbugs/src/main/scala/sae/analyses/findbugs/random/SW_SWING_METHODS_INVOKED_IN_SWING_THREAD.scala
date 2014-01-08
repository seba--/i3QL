package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.constants.OpCodes

/**
 * @author Mirko Köhler, Ralf Mitschke
 */
object SW_SWING_METHODS_INVOKED_IN_SWING_THREAD
	extends (BytecodeDatabase => Relation[BytecodeDatabase#MethodInvocationInstruction]) {


	def apply(database: BytecodeDatabase): Relation[BytecodeDatabase#MethodInvocationInstruction] = {
		import database._
		SELECT (*) FROM database.methodInvocationInstructions WHERE ((i: Rep[MethodInvocationInstruction]) => {
			i.opcode == OpCodes.INVOKEVIRTUAL AND
			i.methodInfo.receiverType.isObjectType AND
			i.methodInfo.receiverType.AsInstanceOf[ObjectType].className.startsWith ("javax/swing/") AND (
				(i.methodInfo.name == "show" AND i.methodInfo.parameterTypes.isEmpty AND i.methodInfo.returnType.isVoidType) OR
				(i.methodInfo.name == "pack" AND i.methodInfo.parameterTypes.isEmpty AND i.methodInfo.returnType.isVoidType) OR
				(i.methodInfo.name == "setVisible" AND i.methodInfo.parameterTypes == Seq (database.boolean) AND i.methodInfo.returnType.isVoidType)
			) AND
			i.declaringMethod.isPublic AND
			i.declaringMethod.isStatic AND
			i.declaringMethod.name == "main" AND
			i.declaringMethod.declaringType.className.toLowerCase.indexOf ("benchmark") >= 0
		})
	}
}
/*
object SW_SWING_METHODS_INVOKED_IN_SWING_THREAD
	extends (BytecodeDatabase => Relation[INVOKEVIRTUAL])
{
	def apply(database: BytecodeDatabase): Relation[INVOKEVIRTUAL] = {
		import database._
		SELECT (*) FROM (invokeVirtual) WHERE
			((i: INVOKEVIRTUAL) => {
				(i.receiverType.isObjectType &&
					i.receiverType.asInstanceOf[ObjectType].className.startsWith ("javax/swing/")
					) && (
					i.name == "show" && i.parameterTypes == Nil && i.returnType == VoidType ||
						i.name == "pack" && i.parameterTypes == Nil && i.returnType == VoidType ||
						i.name == "setVisible" && i.parameterTypes == List (BooleanType) && i.returnType == VoidType
					)
			}) AND
			((i: INVOKEVIRTUAL) => {
				val method: MethodDeclaration = i.declaringMethod
				method.isPublic &&
					method.isStatic &&
					method.name == "main" ||
					method.declaringClassType.className.toLowerCase.indexOf ("benchmark") >= 0
			})
	}
	*/
