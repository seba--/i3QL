package sae.bytecode.model.instructions

import sae.bytecode.model.{MethodDeclaration, Instr, FieldReference}

case class getstatic(declaringMethod: MethodDeclaration, programCounter: Int, field: FieldReference)
        extends Instr[getstatic]
{

    val mnemonic = "getstatic"

    val exceptions = Nil

}
