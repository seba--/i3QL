package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{Instr, Method}

case class `new`(val declaringMethod: Method, val programCounter: Int, val typ: ObjectType)
        extends Instr[`new`] {

    val mnemonic = "new"

    val exceptions = Nil
}
