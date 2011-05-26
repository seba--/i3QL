package sae.bytecode.model.dependencies

import de.tud.cs.st.bat.ReferenceType
import sae.bytecode.model.{Method, Instr}

case class instanceof(source: Method, target: ReferenceType)
        extends Dependency[Method, ReferenceType] {
}