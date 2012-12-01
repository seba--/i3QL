package sae.bytecode.structure

import de.tud.cs.st.bat.resolved.{FieldType, ObjectType}


/**
 * A symbolic reference to a field of a class or an interface
 * is derived from a CONSTANT_Fieldref_info structure (§4.4.2)
 * in the binary representation of a class or interface.
 *
 * Such a reference gives the name and descriptor of the field,
 * as well as a symbolic reference to the class or interface
 * in which the field is to be found.
 */
class FieldReference(val declaringType: ObjectType,
                     val name: String,
                     val fieldType: FieldType)
    extends FieldInfo
{

    override def toString = "FieldReference(" + declaringType.toString + "," + name + "," + fieldType.toString + ")"
}

object FieldReference
{

    def apply(declaringClass: ObjectType,
              name: String,
              fieldType: FieldType) = new FieldReference (declaringClass, name, fieldType)

    def unapply(fieldReference: FieldReference):
    Option[(ObjectType, String, FieldType)] =
        if (fieldReference == null) None
        else Some (
            fieldReference.declaringType,
            fieldReference.name,
            fieldReference.fieldType
        )

}