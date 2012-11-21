package sae.bytecode.model.internal

import de.tud.cs.st.bat.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Created: 21.09.11 13:38
 *
 */

case class unresolved_inner_class_entry(
                                           declaringClass: ObjectType,
                                           innerClassType: ObjectType,
                                           outerClassType: ObjectType,
                                           innerName: String,
                                           accessFlags: Int
                                       )
{

}