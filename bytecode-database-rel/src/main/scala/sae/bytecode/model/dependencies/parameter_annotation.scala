package sae.bytecode.model.dependencies

import sae.bytecode.model.MethodReference
import de.tud.cs.st.bat.ObjectType

case class parameter_annotation(source : MethodReference, target : ObjectType)
        extends Dependency[MethodReference, ObjectType]