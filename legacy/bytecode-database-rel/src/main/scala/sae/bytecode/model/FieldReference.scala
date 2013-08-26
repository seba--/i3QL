package sae.bytecode.model

import de.tud.cs.st.bat.ObjectType

/**
 * A symbolic reference to a field of a class or an interface
 * is derived from a CONSTANT_Fieldref_info structure (§4.4.2)
 * in the binary representation of a class or interface.
 *
 * Such a reference gives the name and descriptor of the field,
 * as well as a symbolic reference to the class or interface
 * in which the field is to be found.
 */
class FieldReference(
                            val declaringClass: ObjectType,
                            val name: String,
                            val fieldType: de.tud.cs.st.bat.FieldType)
        extends FieldIdentifier
{

    override def toString = "FieldReference(" + declaringClass.toString + "," + name + "," + fieldType.toString + ")"
}

object FieldReference
{

    def apply(declaringClass: ObjectType,
              name: String,
              fieldType: de.tud.cs.st.bat.FieldType) = new FieldReference(declaringClass, name, fieldType)

    def unapply(fieldReference: FieldReference):
    Option[(ObjectType, String, de.tud.cs.st.bat.FieldType)] =
        if (fieldReference == null) None
        else Some(
            fieldReference.declaringClass,
            fieldReference.name,
            fieldReference.fieldType
        )

}