package sae.bytecode.model.dependencies

import sae.bytecode.model.Method
import de.tud.cs.st.bat.FieldType

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:55
 *
 */

case class parameter(val source: Method, val target: FieldType)
        extends Dependency[Method, FieldType] {

}