package sae.java.instructions

import sae.java.members.{Field, Method}

case class PutField(declaringMethod: Method, programCounter: Int, field: Field)
        extends Instruction[PutField] {

    val mnemonic = "putfield"

    val exceptions = Nil

}
