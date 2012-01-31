package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{MethodDeclaration, Instr}

case class checkcast(declaringMethod: MethodDeclaration, programCounter: Int, to: ReferenceType)
        extends Instr[checkcast]
{

    val mnemonic = "checkcast"

    val exceptions = Nil
}
