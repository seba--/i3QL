package sae.java.instructions

import sae.java.members.Method

case class InvokeSpecial(declaringMethod: Method, programCounter: Int, method: Method)
        extends Instruction[InvokeSpecial] {

    val mnemonic = "invokespecial"

    val exceptions = Nil

}
