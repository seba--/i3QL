package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ObjectType

case class annotation[SourceType <: AnyRef](source: SourceType, target: ObjectType)
        extends Dependency[SourceType, ObjectType]