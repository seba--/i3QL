package sae.bytecode.model
package instructions

import sae.bytecode.model.Field

case class getfield(val declaringMethod: Method, val programCounter: Int, val field: Field)
        extends Instr[getfield] {

    val mnemonic = "getfield"

    val exceptions = Nil

}
