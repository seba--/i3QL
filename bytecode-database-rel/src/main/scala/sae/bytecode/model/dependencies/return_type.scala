package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.Type
import sae.bytecode.model.MethodDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:57
 *
 */

case class return_type(source: MethodDeclaration, target: Type)
        extends Dependency[MethodDeclaration, Type]
{

}