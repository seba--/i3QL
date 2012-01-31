package sae.bytecode.model
package instructions

import de.tud.cs.st.bat._

case class cast(val declaringMethod: MethodReference, val programCounter: Int, val from: Type, val to: Type)
        extends Instr[cast] {

    val mnemonic = "cast"

    val exceptions = Nil
}
