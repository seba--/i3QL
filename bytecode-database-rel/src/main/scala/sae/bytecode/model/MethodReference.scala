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
class MethodReference(
                             val declaringRef: ReferenceType,
                             val name: String,
                             val parameters: Seq[de.tud.cs.st.bat.Type],
                             val returnType: de.tud.cs.st.bat.Type
                             )
        extends MethodIdentifier
{

    override def toString = "MethodReference(" + declaringRef.toString + "," + name + "," + parameters
            .toString + "," + returnType.toString + ")"
}

object MethodReference
{

    def apply(declaringRef: ReferenceType,
              name: String,
              parameters: Seq[de.tud.cs.st.bat.Type],
              returnType: de.tud.cs.st.bat.Type) = new MethodReference(declaringRef, name, parameters, returnType)

    def unapply(methodReference: MethodReference):
    Option[(ReferenceType, String, Seq[de.tud.cs.st.bat.Type], de.tud.cs.st.bat.Type)] =
        if (methodReference == null) None
        else Some(
            methodReference.declaringRef,
            methodReference.name,
            methodReference.parameters,
            methodReference.returnType
        )

}