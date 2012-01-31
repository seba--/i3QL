package sae.bytecode.model.dependencies

import sae.bytecode.model.MethodReference
import de.tud.cs.st.bat.ObjectType

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:56
 *
 * A dependency arises if a new array of a class type is created.
 * Arrays of primitive types are not a dependency.
 */
case class create_class_array (val source: MethodReference, val target: ObjectType)
        extends Dependency[MethodReference, ObjectType] {

}