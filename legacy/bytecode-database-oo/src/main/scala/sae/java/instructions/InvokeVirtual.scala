package sae.java.instructions

import sae.java.members.Method

case class InvokeVirtual(declaringMethod: Method, programCounter: Int, method: Method)
        extends Instruction[InvokeVirtual]
{

    val mnemonic = "invokevirtual"

    val exceptions = Nil

}
