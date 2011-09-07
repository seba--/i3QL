package sae.bytecode.model.dependencies

import sae.bytecode.model.Method
import de.tud.cs.st.bat.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:54
 *
 */

case class handled_exception(val source: Method, val target: ObjectType)
        extends Dependency[Method, ObjectType] {


}