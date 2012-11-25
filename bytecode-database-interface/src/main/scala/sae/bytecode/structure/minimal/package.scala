package sae.bytecode.structure

import de.tud.cs.st.bat.resolved.{VoidType, Type, ReferenceType, ObjectType}
import sae.bytecode.instructions.minimal.{InvokeInstruction, InstructionInfo}

/**
 *
 * @author Ralf Mitschke
 *
 */
package object minimal
{
    type ClassType = de.tud.cs.st.bat.resolved.ObjectType

    def ClassType(fullyQualified: String): ClassType = de.tud.cs.st.bat.resolved.ObjectType (fullyQualified)

    val void = VoidType

    def declaringType: DeclaredClassMember => ObjectType = _.declaringType

    def sequenceIndex: InstructionInfo => Int = _.sequenceIndex

    def declaringClassType: InstructionInfo => ObjectType = _.declaringMethod.declaringType

    def receiverType: InvokeInstruction => ReferenceType = _.receiverType

    def returnType: MethodInfo => Type = _.returnType

    def classType: ClassDeclaration => ClassType = _.classType

    def subType: InheritanceRelation => ClassType = _.subType

    def superType: InheritanceRelation => ClassType = _.superType

}
