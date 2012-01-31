package sae.bytecode.model.instructions

import sae.bytecode.model.{Instr, MethodReference}

case class invokestatic(val declaringMethod: MethodReference, val programCounter: Int, val method: MethodReference)
        extends Instr[invokestatic] {

    val mnemonic = "invokestatic"

    val exceptions = Nil

}
