package sae.bytecode.model.instructions

import sae.bytecode.model.{Instr, Method}

case class invokevirtual(val declaringMethod: Method, val programCounter: Int, val method: Method)
        extends Instr[invokevirtual] {
    val mnemonic = "invokevirtual"

    val exceptions = Nil
}
