package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{Instr, Method}

case class push[T](val declaringMethod: Method, val programCounter: Int, val value: T, typ: Type)
        extends Instr[push[T]]
{

    val mnemonic = "push"

    val exceptions = Nil

}
