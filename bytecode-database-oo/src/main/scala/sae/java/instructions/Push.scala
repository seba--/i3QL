package sae.java.instructions

import sae.java.members.Method
import sae.java.types.Type

case class Push[T](declaringMethod: Method, programCounter: Int, value: T, typ: Type)
        extends Instruction[Push[T]]
{

    val mnemonic = "push"

    val exceptions = Nil

}
