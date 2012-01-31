package sae.bytecode.model.dependencies

import sae.bytecode.model.MethodReference
import de.tud.cs.st.bat.Type

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:57
 *
 */

case class return_type(source: MethodReference, target: Type)
        extends Dependency[MethodReference, Type]
{

}