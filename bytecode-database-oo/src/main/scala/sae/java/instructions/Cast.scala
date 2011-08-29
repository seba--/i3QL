package sae.java.instructions

import sae.java.members.Method
import sae.java.types.Type

case class Cast(declaringMethod: Method, programCounter: Int, from: Type, to: Type)
        extends Instruction[Cast] {

    val mnemonic = "Cast"

    val exceptions = Nil
}
