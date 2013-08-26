package sae.java.instructions

import sae.java.members.Method
import sae.java.types.ObjectType

case class CheckCast(declaringMethod: Method, programCounter: Int, to: ObjectType)
        extends Instruction[CheckCast]
{

    val mnemonic = "CheckCast"

    val exceptions = Nil
}
