package sae.java.instructions

import sae.java.members.{Method, Field}

case class PutStatic(declaringMethod: Method, programCounter: Int, field: Field)
        extends Instruction[PutStatic] {

    val mnemonic = "putstatic"

    val exceptions = Nil
}
