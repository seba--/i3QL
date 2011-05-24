package sae.bytecode.model.dependencies

import sae.bytecode.model.Field
import de.tud.cs.st.bat.{Type, ObjectType}

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:54
 *
 */

case class field_type(val source: Field, val target: Type)
        extends Dependency[Field, Type] {


}