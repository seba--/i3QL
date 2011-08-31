package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ObjectType

/**
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:46
 */

case class `extends`(val source: ObjectType, val target: ObjectType)
        extends Dependency[ObjectType, ObjectType] {


}