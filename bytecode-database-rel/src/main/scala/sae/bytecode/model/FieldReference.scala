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
case class FieldReference(declaringClass: ObjectType, name: String, fieldType: de.tud.cs.st.bat.FieldType)