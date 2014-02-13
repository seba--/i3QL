package sae.analyses.findbugs

import idb.Relation
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.BytecodeDatabase

/**
 * @author Mirko Köhler
 */
object Definitions {

	def fieldReadsFromExternalPackage(database : BytecodeDatabase) : Relation[BytecodeDatabase#FieldAccessInstruction] = {
		import database._
		SELECT(*) FROM fieldAccessInstructions WHERE (
		(i : Rep[FieldAccessInstruction]) =>
			i.fieldInfo.declaringType.IsInstanceOf[ObjectType] AND
				i.declaringMethod.declaringClass.classType.packageName != i.fieldInfo.declaringType.AsInstanceOf[ObjectType].packageName)
	}


	def ms_fields(database : BytecodeDatabase) : Relation[BytecodeDatabase#FieldDeclaration] = {
		import database._
		SELECT (*) FROM fieldDeclarations WHERE (
			(f : Rep[FieldDeclaration]) =>
				f.isStatic AND
					NOT (f.isSynthetic) AND
					NOT (f.isVolatile) AND
					(f.isProtected OR f.isPublic) AND
					NOT (f.declaringClass.isInterface)
			)
	}


	/*def ms_base(database : BytecodeDatabase): Relation[database.FieldDeclaration] = {
		import database._
		SELECT (*) FROM ms_fields(database) WHERE (
			(fDecl : Rep[FieldDeclaration]) =>
				NOT (
					EXISTS (
						SELECT (*) FROM fieldReadsFromExternalPackage WHERE (
							(fInst : Rep[FieldAccessInstruction]) =>
								fInst.declaringMethod.declaringClass == fDecl.declaringClass AND
									//fInst.fieldInfo.name == "" AND //TODO: Field declaration name
									fInst.fieldInfo.declaringType == fDecl.valueType
							)
					)
				)
			)
	}   */


}
