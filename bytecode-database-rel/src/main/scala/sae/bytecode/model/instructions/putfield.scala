package sae.bytecode.model.instructions

import sae.bytecode.model.{MethodReference, Instr, FieldReference}

case class putfield(val declaringMethod: MethodReference, val programCounter: Int, val field: FieldReference)
        extends Instr[putfield] {

    val mnemonic = "putfield"

    val exceptions = Nil

}
