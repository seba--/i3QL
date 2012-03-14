package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ReferenceType
import sae.bytecode.model.MethodDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:56
 *
 */
case class class_cast(source: MethodDeclaration, target: ReferenceType)
        extends Dependency[MethodDeclaration, ReferenceType]
{

}