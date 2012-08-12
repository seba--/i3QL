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
    extends TypeBindingBAT
    with BytecodeFunctions
    with BytecodeConstants
    with BytecodeSchemaFunctions
{

    def void = VoidType

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

    def returnType = method => method.descriptor.returnType

    def parameterTypes = method => method.descriptor.parameterTypes

    def isSynthetic = field => field.isSynthetic

    def isVolatile = field => field.isVolatile

    def classType = classDeclaration => classDeclaration.thisClass

    //def declaringClass(member: DeclaredClassMember): ClassDeclaration = member.declaringClass

    def declaringClass = (member: DeclaredClassMember) => member.declaringClass
}
