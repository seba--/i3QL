package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.MethodDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:54
 *
 */

case class handled_exception(source: MethodDeclaration, target: ObjectType)
        extends Dependency[MethodDeclaration, ObjectType]
{


}