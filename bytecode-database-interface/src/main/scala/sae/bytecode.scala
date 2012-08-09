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

    def declaringType : MethodDeclaration => ReferenceType = null

    def void = VoidType
/*
    def name : MethodDeclaration => String = method => method.name

    def isPublic : Cla => Boolean = method => method.isPublic

    def returnType : MethodDeclaration => ReturnType = method => method.descriptor.returnType

    def parameters : MethodDeclaration => Seq[Type] = method => method.descriptor.parameterTypes
   */
}
