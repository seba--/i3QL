package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{MethodReference, Instr}

case class checkcast(val declaringMethod: MethodReference, val programCounter: Int, val to: ReferenceType)
        extends Instr[checkcast] {

    val mnemonic = "checkcast"

    val exceptions = Nil
}
