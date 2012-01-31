package sae
package bytecode
package model


import de.tud.cs.st.bat.ReferenceType

/**
 * A symbolic reference to a method of a class
 * is derived from a CONSTANT_Methodref_info structure (§4.4.2)
 * in the binary representation of a class or interface.
 * A symbolic reference to a method of an interface
 * is derived from a CONSTANT_InterfaceMethodref_info structure (§4.4.2)
 * in the binary representation of a class or interface.
 *
 * Such a reference gives the name and descriptor of the method,
 * as well as a symbolic reference to the class/interface in which the method is to be found.
 *
 * MethodReferences may appear on array types, hence the declaringRef is ReferenceType
 * and not ObjectType
 */
case class MethodReference(
                                  declaringRef: ReferenceType,
                                  name: String,
                                  parameters: Seq[de.tud.cs.st.bat.Type],
                                  returnType: de.tud.cs.st.bat.Type
                                  )
{
    def isConstructor = {
        name.startsWith("<init>")
    }

    def isStaticInitializer = {
        name == "<clinit>"
    }
}