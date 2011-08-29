package sae.java.instructions

import sae.java.members.{Method, Field}

case class GetStatic(declaringMethod: Method, programCounter: Int, field: Field)
        extends Instruction[GetStatic]
{

    val mnemonic = "getstatic"

    val exceptions = Nil

}
