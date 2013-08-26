package sae.java.instructions

import sae.java.members.Method

case class InvokeStatic(declaringMethod: Method, programCounter: Int, method: Method)
        extends Instruction[InvokeStatic] {

    val mnemonic = "invokestatic"

    val exceptions = Nil

}
