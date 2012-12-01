package sae.bytecode.structure

import de.tud.cs.st.bat.resolved.{Type, ReferenceType}

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
class MethodReference(val receiverType: ReferenceType,
                      val name: String,
                      val parameterTypes: Seq[Type],
                      val returnType: Type)
    extends MethodInfo
{

    override def toString = "MethodReference(" + receiverType.toString + "," + name + "," + parameterTypes
        .toString + "," + returnType.toString + ")"
}

object MethodReference
{

    def apply(declaringRef: ReferenceType,
              name: String,
              parameters: Seq[Type],
              returnType: Type) = new MethodReference (declaringRef, name, parameters, returnType)

    def unapply(methodReference: MethodReference):
    Option[(ReferenceType, String, Seq[Type], Type)] =
        if (methodReference == null) None
        else Some (
            methodReference.receiverType,
            methodReference.name,
            methodReference.parameterTypes,
            methodReference.returnType
        )

}