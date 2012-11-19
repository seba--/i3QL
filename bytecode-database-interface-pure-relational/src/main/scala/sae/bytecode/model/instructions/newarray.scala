package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{Instr, MethodDeclaration}

case class newarray(declaringMethod: MethodDeclaration, programCounter: Int, typ: Type)
        extends Instr[newarray]
{

    val mnemonic = "newarray"

    val exceptions = Nil

}
