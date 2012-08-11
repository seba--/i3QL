package sae

import de.tud.cs.st.bat.resolved.VoidType

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
{

    def declaringType: MethodDeclaration => ReferenceType = null

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


    def isPublic = member => member.isPublic

    def isPackage = member => !member.isPrivate && !member.isProtected && !member.isPublic

    def isProtected = member => member.isProtected

    def isPrivate = member => member.isPrivate

    def isFinal = member => member.isFinal

    def isStatic = member => member.isStatic

    def returnType = method => method.descriptor.returnType

    def parameterTypes = method => method.descriptor.parameterTypes

    def isSynthetic = field => field.isSynthetic

    def isVolatile = field => field.isVolatile
}
