package sae.bytecode.model.instructions

import sae.bytecode.model.{Method, Instr, Field}

case class putstatic(val declaringMethod: Method, val programCounter: Int, val field: Field)
        extends Instr[putstatic] {

    val mnemonic = "putstatic"

    val exceptions = Nil
}
