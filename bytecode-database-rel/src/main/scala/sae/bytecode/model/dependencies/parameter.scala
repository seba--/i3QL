package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.FieldType
import sae.bytecode.model.MethodDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.05.11 13:55
 *
 */
case class parameter(source: MethodDeclaration, target: FieldType)
        extends Dependency[MethodDeclaration, FieldType]
{

}