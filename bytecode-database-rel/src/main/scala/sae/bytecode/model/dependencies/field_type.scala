package sae.bytecode.model.dependencies

import sae.bytecode.model.FieldReference
import de.tud.cs.st.bat.Type

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:54
 *
 */

case class field_type(source: FieldReference, target: Type)
        extends Dependency[FieldReference, Type]
{

}