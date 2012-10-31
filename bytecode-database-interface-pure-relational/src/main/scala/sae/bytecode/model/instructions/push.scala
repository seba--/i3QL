package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{Instr, MethodDeclaration}

case class push[T](declaringMethod: MethodDeclaration, programCounter: Int, value: T, typ: Type)
        extends Instr[push[T]]
{

    val mnemonic = "push"

    val exceptions = Nil

}
