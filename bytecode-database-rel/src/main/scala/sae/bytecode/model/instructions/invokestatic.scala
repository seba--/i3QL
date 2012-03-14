package sae.bytecode.model.instructions

import sae.bytecode.model.{MethodReference, Instr, MethodDeclaration}


case class invokestatic(declaringMethod: MethodDeclaration, programCounter: Int, method: MethodReference)
        extends Instr[invokestatic]
{

    val mnemonic = "invokestatic"

    val exceptions = Nil

}
