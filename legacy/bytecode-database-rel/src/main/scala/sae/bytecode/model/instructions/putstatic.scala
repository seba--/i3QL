package sae.bytecode.model.instructions

import sae.bytecode.model.{MethodDeclaration, Instr, FieldReference}


case class putstatic(declaringMethod: MethodDeclaration, programCounter: Int, field: FieldReference)
        extends Instr[putstatic]
{

    val mnemonic = "putstatic"

    val exceptions = Nil
}
