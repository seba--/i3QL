package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.MethodDeclaration

case class parameter_annotation(source: MethodDeclaration, target: ObjectType)
        extends Dependency[MethodDeclaration, ObjectType]