package sae.bytecode.model.instructions

import sae.bytecode.model.{Method, Instr, Field}

case class getstatic(val declaringMethod: Method, val programCounter: Int, val field: Field)
        extends Instr[getstatic] {

    val mnemonic = "getstatic"

    val exceptions = Nil

}
