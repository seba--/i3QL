package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.constants.OpCodes


/**
 * @author Mirko Köhler
 */
object UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR
	extends (BytecodeDatabase => Relation[(BytecodeDatabase#FieldAccessInstruction, BytecodeDatabase#MethodInvocationInstruction)]) {

	def apply (database : BytecodeDatabase) : Relation[(BytecodeDatabase#FieldAccessInstruction, BytecodeDatabase#MethodInvocationInstruction)] = {
		import database._

	    val selfCallsFromConstructor : Relation[MethodInvocationInstruction] =
			SELECT (*) FROM methodInvocationInstructions WHERE ((i : Rep[MethodInvocationInstruction]) =>
				i.opcode == OpCodes.INVOKEVIRTUAL AND
				i.declaringMethod.declaringType == i.methodInfo.receiverType AND
				i.declaringMethod.name == "<init>"
			)

		val definedSuperTypes : Relation[MethodInvocationInstruction] =
			SELECT (*) FROM methodInvocationInstructions WHERE ((i : Rep[MethodInvocationInstruction]) =>
				i.opcode == OpCodes.INVOKESPECIAL AND
				i.declaringMethod.name == "<init>" AND
				i.methodInfo.name == "<init>" AND
				i.declaringMethod.declaringClass.superType.isDefined
			)

		val superCalls : Relation[MethodInvocationInstruction] =
			SELECT (*) FROM definedSuperTypes WHERE ((i : Rep[MethodInvocationInstruction]) =>
				i.declaringMethod.declaringClass.superType.get == i.methodInfo.receiverType
			)

		val selfCallsFromCalledConstructor : Relation[MethodInvocationInstruction] =
			SELECT (*) FROM selfCallsFromConstructor WHERE ((i1 : Rep[MethodInvocationInstruction]) =>
				EXISTS (
					SELECT (*) FROM superCalls WHERE ((i2 : Rep[MethodInvocationInstruction]) =>
						i2.methodInfo.receiverType == i1.declaringMethod.declaringType AND
						i2.methodInfo.name == i1.declaringMethod.name AND
						i2.methodInfo.returnType == i1.declaringMethod.returnType AND
						i2.methodInfo.parameterTypes == i1.declaringMethod.parameterTypes
					)
				)
			)

		val selfFieldReads : Relation[FieldAccessInstruction] =
			SELECT ((get: Rep[FieldAccessInstruction], f: Rep[FieldDeclaration]) => get) FROM (fieldAccessInstructions, fieldDeclarations) WHERE ((i : Rep[FieldAccessInstruction], d : Rep[FieldDeclaration]) =>
				i.opcode == OpCodes.GETFIELD AND
				i.declaringMethod.declaringType == i.fieldInfo.declaringType AND
				NOT (i.declaringMethod.name == "<init>") AND
				i.fieldInfo.declaringType == d.declaringClass.classType AND
				i.fieldInfo.name == d.name AND
				i.fieldInfo.fieldType == d.fieldType AND
				i.declaringMethod.declaringClass.superType.isDefined
			)


		SELECT (*) FROM (selfFieldReads, selfCallsFromCalledConstructor) WHERE ((fai : Rep[FieldAccessInstruction], mii : Rep[MethodInvocationInstruction]) =>
			(fai.declaringMethod.declaringClass.superType.get == mii.methodInfo.receiverType) AND
			(fai.declaringMethod.name == mii.methodInfo.name) AND
			(fai.declaringMethod.returnType == mii.methodInfo.returnType) AND
			(fai.declaringMethod.parameterTypes == mii.methodInfo.parameterTypes)
		)

	}

}


/*
object UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR
	extends (BytecodeDatabase => Relation[(GETFIELD, InvokeInstruction)])
{
	def apply(database: BytecodeDatabase): Relation[(GETFIELD, InvokeInstruction)] = {
		import database._

		val selfCallsFromConstructor = compile (
			SELECT (*) FROM invokeVirtual.asInstanceOf[Relation[InvokeInstruction]] WHERE
				(i => i.declaringMethod.declaringClassType == i.receiverType) AND
				(_.declaringMethod.name == "<init>")
		)

		assert (!sae.ENABLE_FORCE_TO_SET || selfCallsFromConstructor.isSet)

		val superCalls = compile (
			SELECT (*) FROM invokeSpecial WHERE
				(_.declaringMethod.name == "<init>") AND
				(_.name == "<init>") AND
				(_.declaringMethod.declaringClass.superClass.isDefined) AND
				(i => i.declaringMethod.declaringClass.superClass.get == i.receiverType)
		)

		assert (!sae.ENABLE_FORCE_TO_SET || superCalls.isSet)

		val selfCallsFromCalledConstructor = compile (
			SELECT (*) FROM (selfCallsFromConstructor) WHERE EXISTS (
				SELECT (*) FROM (superCalls) WHERE
					(((_: INVOKESPECIAL).receiverType) === ((_: InvokeInstruction).declaringMethod.declaringClassType)) AND
					(((_: INVOKESPECIAL).name) === ((_: InvokeInstruction).declaringMethod.name)) AND
					(((_: INVOKESPECIAL).returnType) === ((_: InvokeInstruction).declaringMethod.returnType)) AND
					(((_: INVOKESPECIAL).parameterTypes) === ((_: InvokeInstruction).declaringMethod.parameterTypes))
			)
		)

		val selfFieldReads = compile (
			SELECT ((get: GETFIELD, f: FieldDeclaration) => get) FROM (getField, fieldDeclarations) WHERE
				(i => i.declaringMethod.declaringClassType == i.receiverType) AND
				(_.declaringMethod.name != "<init>") AND
				(((_: GETFIELD).receiverType) === ((_: FieldDeclaration).declaringType)) AND
				(((_: GETFIELD).name) === ((_: FieldDeclaration).name)) AND
				(((_: GETFIELD).fieldType) === ((_: FieldDeclaration).fieldType))
		)

		compile (
			SELECT (*) FROM (selfFieldReads, selfCallsFromCalledConstructor) WHERE
				(((_: GETFIELD).declaringMethod.declaringClass.superClass.get) === ((_: InvokeInstruction).receiverType)) AND
				(((_: GETFIELD).declaringMethod.name) === ((_: InvokeInstruction).name)) AND
				(((_: GETFIELD).declaringMethod.returnType) === ((_: InvokeInstruction).returnType)) AND
				(((_: GETFIELD).declaringMethod.parameterTypes) === ((_: InvokeInstruction).parameterTypes))

		)

	}
}*/

