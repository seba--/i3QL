package sae.bytecode.model.dependencies

import sae.bytecode.model.{FieldReference, MethodReference}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:56
 *
 */

case class write_field (source: MethodReference, target: FieldReference, isStatic: Boolean)
        extends Dependency[MethodReference, FieldReference] {

}