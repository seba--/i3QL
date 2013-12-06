package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.constants.AccessFlags

/**
 * @author Mirko Köhler
 */
object SE_BAD_FIELD_INNER_CLASS
	extends (BytecodeDatabase => Relation[(BytecodeDatabase#ObjectType, BytecodeDatabase#ObjectType)]) {


	def apply(database : BytecodeDatabase): Relation[(BytecodeDatabase#ObjectType, BytecodeDatabase#ObjectType)] = {
		import database._

		val nonStaticInner : Relation[(Type, Type)] =
			SELECT ((a: Rep[InnerClassAttribute]) => (a.innerClassType, a.outerClassType.get)) FROM innerClassAttributes WHERE
				((a : Rep[InnerClassAttribute]) =>
					a.declaringClass.classType == a.innerClassType AND
					a.outerClassType.isDefined AND
					(a.innerClassAccessFlags & AccessFlags.ACC_STATIC) != 0)




		return null
	}

	/*def hasStaticFlag: UnresolvedInnerClassEntry => Boolean = e =>
		ACC_STATIC.element_of (e.accessFlags)

	def innerClass: ((ObjectType, ObjectType)) => ObjectType = _._1

	def outerClass: ((ObjectType, ObjectType)) => ObjectType = _._2

	def apply(database: BytecodeDatabase): Relation[(ObjectType, ObjectType)] = {
		import database._
		val definitions = Definitions (database)
		import definitions._

		lazy val nonStaticInner = compile (
			SELECT ((u: UnresolvedInnerClassEntry) => (u.innerClassType, u.outerClassType.get)) FROM unresolvedInnerClasses WHERE
				((u:UnresolvedInnerClassEntry) => u.declaringType == u.innerClassType) AND
				(_.outerClassType.isDefined) AND
				NOT (hasStaticFlag)
		)

		lazy val directlySerializable = compile (
			SELECT (subType) FROM interfaceInheritance WHERE (_.superType == serializable)
		)

		lazy val serializableNonStaticInner = compile (
			SELECT ((e: (ObjectType, ObjectType), s: ObjectType) => e) FROM (nonStaticInner, directlySerializable) WHERE
				(innerClass === thisClass)
		)

		SELECT (*) FROM serializableNonStaticInner WHERE NOT (
			EXISTS (
				SELECT (*) FROM subTypesOfSerializable WHERE
					(thisClass === outerClass)
			)
		)

	} */

}
