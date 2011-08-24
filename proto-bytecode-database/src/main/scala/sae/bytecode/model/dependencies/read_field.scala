package sae.bytecode.model.dependencies

import sae.bytecode.model.{Field, Method}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:56
 *
 */

case class read_field (source: Method, target: Field, isStatic: Boolean)
        extends Dependency[Method, Field] {

}