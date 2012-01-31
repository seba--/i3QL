package sae.bytecode.model.instructions

import de.tud.cs.st.bat._
import sae.bytecode.model.{Instr, MethodDeclaration}

case class `new`(declaringMethod: MethodDeclaration, programCounter: Int, typ: ObjectType)
        extends Instr[`new`]
{

    val mnemonic = "new"

    val exceptions = Nil
}
