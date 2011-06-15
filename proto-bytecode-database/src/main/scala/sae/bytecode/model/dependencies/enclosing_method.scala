package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.Method

/**
 * 
 * Author: Ralf Mitschke
 * Created: 09.06.11 11:19
 *
 */

case class enclosing_method(source : Method, target : ObjectType)
    extends Dependency[Method, ObjectType]