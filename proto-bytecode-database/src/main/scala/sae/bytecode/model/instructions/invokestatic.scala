package sae.bytecode.model.instructions

import sae.bytecode.model.{Instr, Method}

case class invokestatic(val declaringMethod: Method, val programCounter: Int, val method: Method)
        extends Instr[invokestatic] {

    val mnemonic = "invokestatic"

    val exceptions = Nil

}
