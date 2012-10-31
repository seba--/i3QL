package sae.bytecode.model.instructions

import sae.bytecode.model.{MethodDeclaration, Instr, FieldReference}


case class putfield(declaringMethod: MethodDeclaration, programCounter: Int, field: FieldReference)
        extends Instr[putfield]
{

    val mnemonic = "putfield"

    val exceptions = Nil

}
