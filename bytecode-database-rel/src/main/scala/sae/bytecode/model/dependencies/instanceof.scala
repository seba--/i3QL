package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ReferenceType
import sae.bytecode.model.{MethodReference, Instr}

case class instanceof(source: MethodReference, target: ReferenceType)
        extends Dependency[MethodReference, ReferenceType] {
}