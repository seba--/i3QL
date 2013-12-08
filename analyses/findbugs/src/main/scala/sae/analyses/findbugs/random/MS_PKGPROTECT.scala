package sae.analyses.findbugs.random

import sae.bytecode.BytecodeDatabase
import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Ralf Mitschke, Mirko Köhler
 */
object MS_PKGPROTECT extends (BytecodeDatabase => Relation[BytecodeDatabase#FieldDeclaration])
{

	def apply(database: BytecodeDatabase): Relation[BytecodeDatabase#FieldDeclaration] = {
		import database._

		val fieldReadsFromExternalPackage: Relation[FieldAccessInstruction] =
			SELECT(*) FROM fieldAccessInstructions WHERE (
				(i : Rep[FieldAccessInstruction]) =>
					i.fieldInfo.declaringType.IsInstanceOf[ObjectType] AND
					i.declaringMethod.declaringClass.classType.packageName != i.fieldInfo.declaringType.AsInstanceOf[ObjectType].packageName)

		val ms_fields: Relation[FieldDeclaration] =
			SELECT (*) FROM fieldDeclarations WHERE (
				(f : Rep[FieldDeclaration]) =>
					f.isStatic AND
					NOT (f.isSynthetic) AND
					NOT (f.isVolatile) AND
					(f.isProtected OR f.isPublic) AND
					NOT (f.declaringClass.isInterface)
			)



		val ms_base: Relation[FieldDeclaration] =
			SELECT (*) FROM ms_fields WHERE (
				(fDecl : Rep[FieldDeclaration]) =>
					NOT (
						EXISTS (
							SELECT (*) FROM fieldReadsFromExternalPackage WHERE (
								(fInst : Rep[FieldAccessInstruction]) =>
									fInst.declaringMethod.declaringClass == fDecl.declaringClass AND
									//fInst.fieldInfo.name == "" AND
									// TODO: Field declaration name
									fInst.fieldInfo.declaringType == fDecl.valueType
							)
						)
					)
			)

		SELECT (*) FROM ms_base WHERE (
			(f : Rep[FieldDeclaration]) =>
		        f.isFinal AND (f.valueType.IsInstanceOf[ArrayType[Any]] OR f.valueType == ObjectType ("java/util/Hashtable"))
		)

	}


}
