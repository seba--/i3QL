package sae.java.instructions

import sae.java.members.Method

case class InvokeInterface(declaringMethod: Method, programCounter: Int, method: Method)
        extends Instruction[InvokeInterface] {

    val mnemonic = "invokeinterface"

    val exceptions = Nil

}
