package sae.bytecode.model.instructions

import de.tud.cs.st.bat.ReferenceType
import sae.bytecode.model.{MethodReference, Instr}

case class instanceof(val declaringMethod: MethodReference, val programCounter: Int, val typ: ReferenceType)
        extends Instr[instanceof] {

    val mnemonic = "instanceof"

    val exceptions = Nil
}