package sae
package bytecode
package model


import de.tud.cs.st.bat.ReferenceType

/**
 *  Methods can either called on arrays or on objects.
 *  Thus they are declared as ReferenceType
 */
case class Method(declaringRef : ReferenceType, name : String, parameters : Seq[de.tud.cs.st.bat.Type], returnType : de.tud.cs.st.bat.Type)