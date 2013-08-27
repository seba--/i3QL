package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.MethodDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:56
 *
 */

case class throws(source: MethodDeclaration, target: ObjectType)
        extends Dependency[MethodDeclaration, ObjectType]
{

}