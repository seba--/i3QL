package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{Instr, Method}

case class newarray(val declaringMethod: Method, val programCounter: Int, val typ: Type)
        extends Instr[newarray] {

    val mnemonic = "newarray"

    val exceptions = Nil

}
