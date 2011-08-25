package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{Method, Instr}

case class checkcast(val declaringMethod: Method, val programCounter: Int, val to: ReferenceType)
        extends Instr[checkcast] {

    val mnemonic = "checkcast"

    val exceptions = Nil
}
