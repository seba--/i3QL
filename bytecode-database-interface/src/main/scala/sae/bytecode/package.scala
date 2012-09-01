package sae

import de.tud.cs.st.bat.resolved.VoidType
import de.tud.cs.st.bat.{ACC_PROTECTED, ACC_FINAL, ACC_PRIVATE, ACC_PUBLIC}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 09.08.12
 * Time: 22:40
 */

package object bytecode
{

    type Instruction = de.tud.cs.st.bat.resolved.Instruction

    type InstructionInfo = {
        def instruction: de.tud.cs.st.bat.resolved.Instruction
        def bytecodeIndex: Int
        def sequenceIndex: Int
    }

    type Type = de.tud.cs.st.bat.resolved.Type

    type ReturnType = de.tud.cs.st.bat.resolved.Type

    type ParameterType = de.tud.cs.st.bat.resolved.FieldType

    type FieldType = de.tud.cs.st.bat.resolved.FieldType

    type VoidType = de.tud.cs.st.bat.resolved.VoidType

    type ReferenceType = de.tud.cs.st.bat.resolved.ReferenceType

    type ClassType = de.tud.cs.st.bat.resolved.ObjectType

    type ArrayType = de.tud.cs.st.bat.resolved.ArrayType

    type InterfaceType = de.tud.cs.st.bat.resolved.ObjectType

    type PrimitiveType = de.tud.cs.st.bat.resolved.BaseType

    type AccessModified = {def accessFlags: Int}

    type ClassMember = AnyRef

    type DeclaredClassMember = ClassMember {
        def declaringClass: ClassDeclaration

        def isPublic: Boolean

        def isProtected: Boolean

        def isPrivate: Boolean

        def isStatic: Boolean
    }

    type ClassDeclaration = AnyRef with AccessModified {
        def classType: ClassType

        def isFinal: Boolean
    }

    type InterfaceDeclaration = de.tud.cs.st.bat.resolved.ClassFile with AccessModified

    type FieldInfo = ClassMember {
        def name: String

        def fieldType: FieldType
    }

    type MethodInfo = ClassMember {
        def name: String

        def returnType: ReturnType

        def parameterTypes: Seq[ParameterType]
    }

    type MethodDeclaration = DeclaredClassMember with MethodInfo

    type FieldDeclaration = DeclaredClassMember with FieldInfo {
        def isFinal: Boolean

        def isTransient: Boolean

        def isVolatile: Boolean

        def isSynthetic: Boolean

        def isEnum: Boolean
    }

    type InheritanceRelation = AnyRef {
        def superType: ClassType

        def subType: ClassType
    }


    type SourceElement = de.tud.cs.st.bat.resolved.SourceElement

    type ReadFieldInstruction = de.tud.cs.st.bat.resolved.Instruction {def declaringMethod: MethodDeclaration; def targetField: FieldInfo}

    val void = VoidType

/*
    def name = member => {
        if (member.isInstanceOf[MethodDeclaration]) {
            member.asInstanceOf[MethodDeclaration].name
        }
        else
        if (member.isInstanceOf[FieldDeclaration]) {
            member.asInstanceOf[FieldDeclaration].name
        }
        else
            throw new UnsupportedOperationException ("Object " + member + " of type " + member.getClass + " has no name attribute")
    }


    def isPublic(modifiedElement: AccessModified) = ACC_PUBLIC element_of modifiedElement.accessFlags

    def isPackage = member => !isPublic (member) && !(ACC_PRIVATE element_of member.accessFlags) && !(ACC_PROTECTED element_of member.accessFlags)

    def isProtected = member => member.isProtected

    def isPrivate = member => member.isPrivate

    def isFinal = member => ACC_FINAL element_of member.accessFlags

    def isStatic = member => member.isStatic

    def returnType = method => method.returnType

    def parameterTypes = method => method.parameterTypes

    def isSynthetic = field => field.isSynthetic

    def isVolatile = field => field.isVolatile

    def classType = classDeclaration => classDeclaration.classType

    def declaringClass = _.declaringClass
*/

    def classType: ClassDeclaration => ClassType = classDeclaration => classDeclaration.classType
}
