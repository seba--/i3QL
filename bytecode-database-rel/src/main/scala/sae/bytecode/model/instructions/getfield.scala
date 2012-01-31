package sae.bytecode.model
package instructions

import sae.bytecode.model.FieldReference

case class getfield(val declaringMethod: MethodReference, val programCounter: Int, val field: FieldReference)
        extends Instr[getfield] {

    val mnemonic = "getfield"

    val exceptions = Nil

}
