package sae.bytecode.model
package instructions

import sae.bytecode.model.Method

case class invokeinterface(val declaringMethod: Method, val programCounter: Int, val method: Method)
        extends Instr[invokeinterface] {

    val mnemonic = "invokeinterface"

    val exceptions = Nil

}
