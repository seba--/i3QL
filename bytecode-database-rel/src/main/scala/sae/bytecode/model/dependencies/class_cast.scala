package sae.bytecode.model.dependencies

import sae.bytecode.model.{Field, Method}
import de.tud.cs.st.bat.ReferenceType

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:56
 *
 */
case class class_cast (val source: Method, val target: ReferenceType)
        extends Dependency[Method, ReferenceType] {

}