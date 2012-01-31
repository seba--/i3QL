package sae.bytecode.model.instructions

import sae.bytecode.model.{Instr, MethodReference}

case class invokespecial(val declaringMethod: MethodReference, val programCounter: Int, val method: MethodReference)
        extends Instr[invokespecial] {

    val mnemonic = "invokespecial"

    val exceptions = Nil

}
