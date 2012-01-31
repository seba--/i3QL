package sae.bytecode.model.dependencies

import sae.bytecode.model.MethodReference
import de.tud.cs.st.bat.FieldType

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:55
 *
 */

case class parameter(val source: MethodReference, val target: FieldType)
        extends Dependency[MethodReference, FieldType] {

}