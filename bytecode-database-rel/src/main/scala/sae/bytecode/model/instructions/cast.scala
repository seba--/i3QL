package sae.bytecode.model
package instructions

import de.tud.cs.st.bat._

case class cast(declaringMethod: MethodDeclaration, programCounter: Int, from: Type, to: Type)
        extends Instr[cast]
{

    val mnemonic = "cast"

    val exceptions = Nil
}
