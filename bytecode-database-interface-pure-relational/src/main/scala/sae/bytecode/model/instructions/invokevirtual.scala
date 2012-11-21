package sae.bytecode.model.instructions

import sae.bytecode.model.{MethodReference, Instr, MethodDeclaration}


case class invokevirtual(declaringMethod: MethodDeclaration, programCounter: Int, method: MethodReference)
        extends Instr[invokevirtual]
{
    val mnemonic = "invokevirtual"

    val exceptions = Nil
}
