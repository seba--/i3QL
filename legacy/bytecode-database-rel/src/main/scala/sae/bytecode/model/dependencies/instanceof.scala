package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ReferenceType
import sae.bytecode.model.MethodDeclaration

case class instanceof(source: MethodDeclaration, target: ReferenceType)
        extends Dependency[MethodDeclaration, ReferenceType]
{
}