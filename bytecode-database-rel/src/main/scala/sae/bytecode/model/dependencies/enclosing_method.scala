package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.MethodReference

/**
 * 
 * Author: Ralf Mitschke
 * Created: 09.06.11 11:19
 *
 */

case class enclosing_method(source : MethodReference, target : ObjectType)
    extends Dependency[MethodReference, ObjectType]