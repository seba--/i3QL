package sae.java.instructions

import sae.java.members.Method
import sae.java.types.ObjectType

case class InstanceOf(declaringMethod: Method, programCounter: Int, Type: ObjectType)
        extends Instruction[InstanceOf]
{

    val mnemonic = "instanceof"

    val exceptions = Nil

}