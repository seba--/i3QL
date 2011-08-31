package sae.bytecode.model.instructions

import sae.bytecode.model.{Method, Instr, Field}

case class putfield(val declaringMethod: Method, val programCounter: Int, val field: Field)
        extends Instr[putfield] {

    val mnemonic = "putfield"

    val exceptions = Nil

}
