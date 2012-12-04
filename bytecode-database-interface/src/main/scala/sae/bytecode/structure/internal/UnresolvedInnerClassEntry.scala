package sae.bytecode.structure.internal

import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Created: 21.09.11 13:38
 *
 */

case class UnresolvedInnerClassEntry(declaringType: ObjectType,
                                     innerClassType: ObjectType,
                                     outerClassType: Option[ObjectType],
                                     innerName: Option[String],
                                     accessFlags: Int)
{

}