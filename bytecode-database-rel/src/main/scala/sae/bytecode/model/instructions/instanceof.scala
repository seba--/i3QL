package sae.bytecode.model.instructions

import de.tud.cs.st.bat.ReferenceType
import sae.bytecode.model.{MethodDeclaration, Instr}

case class instanceof(declaringMethod: MethodDeclaration, programCounter: Int, typ: ReferenceType)
        extends Instr[instanceof]
{

    val mnemonic = "instanceof"

    val exceptions = Nil
}