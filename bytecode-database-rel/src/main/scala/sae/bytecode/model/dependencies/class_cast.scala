package sae.bytecode.model.dependencies

import sae.bytecode.model.{FieldReference, MethodReference}
import de.tud.cs.st.bat.ReferenceType

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:56
 *
 */
case class class_cast (val source: MethodReference, val target: ReferenceType)
        extends Dependency[MethodReference, ReferenceType] {

}