package sae.bytecode.model.dependencies

import sae.bytecode.model.Method
import de.tud.cs.st.bat.Type

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:57
 *
 */

case class return_type(val source: Method, val target: Type)
        extends Dependency[Method, Type] {

}