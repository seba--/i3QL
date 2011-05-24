package sae.bytecode.model.instructions

import sae.bytecode.model.{Instr, Method}

case class invokespecial(val declaringMethod: Method, val programCounter: Int, val method: Method)
        extends Instr[invokespecial] {

    val mnemonic = "invokespecial"

    val exceptions = Nil

}
