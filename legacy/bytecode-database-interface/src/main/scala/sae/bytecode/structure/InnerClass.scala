package sae.bytecode.structure

import de.tud.cs.st.bat.resolved.ObjectType
import internal.UnresolvedInnerClassEntry

/**
 *
 * Author: Ralf Mitschke
 * Created: 07.06.11 16:53
 *
 * An inner class can be a declared of a class but may also be declared inside a method
 * A class can also be anonymous
 */
case class InnerClass(outerType: ObjectType,
                      classType: ObjectType,
                      isDeclaredMember: Boolean,
                      name: Option[String])
{
    def this (entry: UnresolvedInnerClassEntry) =
        this (
            entry.outerClassType.get,
            entry.innerClassType,
            true,
            entry.innerName
        )
}
