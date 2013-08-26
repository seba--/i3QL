package sae.java.instructions

import sae.java.members.{Method, Field}

case class GetField(declaringMethod: Method, programCounter: Int, field: Field)
        extends Instruction[GetField]
{

    val mnemonic = "getfield"

    val exceptions = Nil

}
