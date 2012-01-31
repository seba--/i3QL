package sae.bytecode.model.instructions

import sae.bytecode.model.{MethodReference, Instr, FieldReference}

case class getstatic(val declaringMethod: MethodReference, val programCounter: Int, val field: FieldReference)
        extends Instr[getstatic] {

    val mnemonic = "getstatic"

    val exceptions = Nil

}
