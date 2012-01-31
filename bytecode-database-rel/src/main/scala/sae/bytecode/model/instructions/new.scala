package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{Instr, MethodReference}

case class `new`(val declaringMethod: MethodReference, val programCounter: Int, val typ: ObjectType)
        extends Instr[`new`] {

    val mnemonic = "new"

    val exceptions = Nil
}
