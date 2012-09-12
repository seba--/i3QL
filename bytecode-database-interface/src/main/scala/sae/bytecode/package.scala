package sae

import bytecode.instructions.{InstructionInfo, InvokeInstruction}
import bytecode.structure.{MethodInfo, MethodDeclaration, ClassDeclaration, DeclaredClassMember}
import de.tud.cs.st.bat.resolved.{Type, ReferenceType, VoidType}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 09.08.12
 * Time: 22:40
 */

package object bytecode
{

    type ClassType = de.tud.cs.st.bat.resolved.ObjectType

    def ClassType(fullyQualified: String): ClassType = de.tud.cs.st.bat.resolved.ObjectType (fullyQualified)

    val void = VoidType


    def declaringClass: DeclaredClassMember => ClassDeclaration = _.declaringClass

    def declaringMethod: InstructionInfo => MethodDeclaration = _.declaringMethod

    def sequenceIndex: InstructionInfo => Int = _.sequenceIndex

    def receiverType: InvokeInstruction => ReferenceType = _.receiverType

    def returnType: MethodInfo => Type = _.returnType

    def classType: ClassDeclaration => ClassType = _.classType
}
