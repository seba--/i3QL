package sae.bytecode.model.dependencies

import sae.bytecode.model.Method
import de.tud.cs.st.bat.ObjectType

case class parameter_annotation(source : Method, target : ObjectType)
        extends Dependency[Method, ObjectType]