package sae.bytecode.model.internal

import de.tud.cs.st.bat.{Type, ObjectType}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 09.06.11 13:49
 *
 * an enclosing method may not point to a method,
 * in case the anonymous inner class is immediately assigned to an instance field.
 * Conceptually the class is also not declared in the constructor
 */
case class unresolved_enclosing_method(declaringClass : ObjectType,
                                       name : Option[String],
                                       parameters : Option[Seq[Type]],
                                       returnType : Option[Type],
                                       innerClass: ObjectType
)