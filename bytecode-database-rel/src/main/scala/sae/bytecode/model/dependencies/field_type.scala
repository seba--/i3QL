package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.Type
import sae.bytecode.model.FieldDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:54
 *
 */

case class field_type(source: FieldDeclaration, target: Type)
        extends Dependency[FieldDeclaration, Type]
{

}