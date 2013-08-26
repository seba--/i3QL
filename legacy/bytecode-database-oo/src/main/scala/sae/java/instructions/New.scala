package sae.java.instructions

import sae.java.members.Method
import sae.java.types.ObjectType

case class New(declaringMethod: Method, programCounter: Int, typ: ObjectType)
        extends Instruction[New] {

    val mnemonic = "new"

    val exceptions = Nil
}
