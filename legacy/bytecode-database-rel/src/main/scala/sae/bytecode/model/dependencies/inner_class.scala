package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ObjectType
/**
 * 
 * Author: Ralf Mitschke
 * Created: 07.06.11 16:53
 *
 * An inner class can be a declared of a class but may also be declared inside a method
 * A class can also be anonymous
 */
case class inner_class(val source: ObjectType, val target: ObjectType, isDeclaredMember : Boolean, name : Option[String])
        extends Dependency[ObjectType, ObjectType] {


}