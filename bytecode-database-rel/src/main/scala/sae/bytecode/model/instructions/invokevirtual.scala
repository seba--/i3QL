package sae.bytecode.model.instructions

import sae.bytecode.model.{Instr, MethodReference}

case class invokevirtual(val declaringMethod: MethodReference, val programCounter: Int, val method: MethodReference)
        extends Instr[invokevirtual] {
    val mnemonic = "invokevirtual"

    val exceptions = Nil
}
