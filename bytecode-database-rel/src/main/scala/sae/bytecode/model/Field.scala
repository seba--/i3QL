package sae.bytecode.model

import de.tud.cs.st.bat.{ObjectType, ReferenceType}

/**
 *  Methods can either called on arrays or on objects.
 *  Thus they are declared as ReferenceType
 */
case class Field(declaringClass: ObjectType, name: String, fieldType: de.tud.cs.st.bat.FieldType)