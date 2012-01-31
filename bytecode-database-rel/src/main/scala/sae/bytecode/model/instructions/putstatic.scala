package sae.bytecode.model.instructions

import sae.bytecode.model.{MethodReference, Instr, FieldReference}

case class putstatic(val declaringMethod: MethodReference, val programCounter: Int, val field: FieldReference)
        extends Instr[putstatic] {

    val mnemonic = "putstatic"

    val exceptions = Nil
}
