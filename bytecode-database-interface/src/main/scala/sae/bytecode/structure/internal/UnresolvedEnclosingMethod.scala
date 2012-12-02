package sae.bytecode.structure.internal

import de.tud.cs.st.bat.resolved.{Type, FieldType, ObjectType}

/**
 *
 * Author: Ralf Mitschke
 * Created: 09.06.11 13:49
 *
 * an enclosing method may not point to a method,
 * in case the anonymous inner class is immediately assigned to an instance field.
 * Conceptually the class is also not declared in the constructor
 */
case class UnresolvedEnclosingMethod(declaringClass: ObjectType,
                                     name: Option[String],
                                     parameterTypes: Option[Seq[FieldType]],
                                     returnType: Option[Type],
                                     innerClass: ObjectType)