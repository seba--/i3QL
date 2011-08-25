package sae.bytecode.model.instructions

import de.tud.cs.st.bat.ReferenceType
import sae.bytecode.model.{Method, Instr}

case class instanceof(val declaringMethod: Method, val programCounter: Int, val typ: ReferenceType)
        extends Instr[instanceof] {

    val mnemonic = "instanceof"

    val exceptions = Nil
}