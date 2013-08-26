package sae.java.instructions

import sae.java.members.Method
import sae.java.types.Type

case class NewArray(declaringMethod: Method, programCounter: Int, typ: Type)
        extends Instruction[NewArray] {

    val mnemonic = "newarray"

    val exceptions = Nil

}
