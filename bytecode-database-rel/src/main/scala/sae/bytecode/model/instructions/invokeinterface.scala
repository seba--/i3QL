package sae.bytecode.model
package instructions

import sae.bytecode.model.MethodReference

case class invokeinterface(val declaringMethod: MethodReference, val programCounter: Int, val method: MethodReference)
        extends Instr[invokeinterface] {

    val mnemonic = "invokeinterface"

    val exceptions = Nil

}
