package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.constants.OpCodes



/**
 * @author Mirko Köhler, Ralf Mitschke
 */
object UG_SYNC_SET_UNSYNC_GET
	extends (BytecodeDatabase => Relation[(BytecodeDatabase#MethodDeclaration, BytecodeDatabase#MethodDeclaration)]) {

	def apply(database : BytecodeDatabase) : Relation[(BytecodeDatabase#MethodDeclaration, BytecodeDatabase#MethodDeclaration)] = {
		import database._

		val syncedSetters : Relation[MethodDeclaration] =
			SELECT (*) FROM (methodDeclarations) WHERE ((m : Rep[MethodDeclaration]) =>
				NOT (m.isAbstract) AND
				NOT (m.isStatic) AND
			    NOT (m.isNative) AND
				NOT (m.isPrivate) AND
				m.name.startsWith ("set") AND
				m.isSynchronized AND
				m.parameterTypes.length == 1 AND
				m.returnType.isVoidType AND
				NOT (m.declaringClass.isInterface)
			)

		val unsyncedGetters : Relation[MethodDeclaration] =
			SELECT (*) FROM (methodDeclarations) WHERE ((m : Rep[MethodDeclaration]) =>
				NOT (m.isAbstract) AND
				NOT (m.isStatic) AND
				NOT (m.isNative) AND
				NOT (m.isPrivate) AND
				m.name.startsWith ("get") AND
				NOT (m.isSynchronized) AND
				m.parameterTypes.length == 0 AND
				NOT (m.returnType.isVoidType) AND
				NOT (m.declaringClass.isInterface)
			)

		SELECT (*) FROM (syncedSetters, unsyncedGetters) WHERE ((setter : Rep[MethodDeclaration], getter : Rep[MethodDeclaration]) =>
			setter.declaringType == getter.declaringType AND
			setter.name.substring (3) == getter.name.substring (3)
		)
	}

}

/*def setterName: MethodDeclaration => String = _.name.substring (3)

def getterName: MethodDeclaration => String = _.name.substring (3)

def apply(database: BytecodeDatabase): Relation[(MethodDeclaration, MethodDeclaration)] = {
import database._

val syncedSetters: Relation[MethodDeclaration] =
SELECT (*) FROM (methodDeclarations) WHERE
(!_.isAbstract) AND
(!_.isStatic) AND
(!_.isNative) AND
(!_.isPrivate) AND
(_.name.startsWith ("set")) AND
(_.isSynchronized) AND
(_.parameterTypes.length == 1) AND
(_.returnType == VoidType) AND
(!_.declaringClass.isInterface)

val unsyncedGetters: Relation[MethodDeclaration] =
SELECT (*) FROM (methodDeclarations) WHERE
(!_.isAbstract) AND
(!_.isStatic) AND
(!_.isNative) AND
(!_.isPrivate) AND
(_.name.startsWith ("get")) AND
(!_.isSynchronized) AND
(_.parameterTypes == Nil) AND
(_.returnType != VoidType) AND
(!_.declaringClass.isInterface)

SELECT (*) FROM (syncedSetters, unsyncedGetters) WHERE
(declaringType === declaringType) AND
(setterName === getterName)
}
*/