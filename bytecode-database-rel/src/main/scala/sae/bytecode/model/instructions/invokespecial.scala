package sae.bytecode.model.instructions

import sae.bytecode.model.{MethodReference, Instr, MethodDeclaration}


case class invokespecial(declaringMethod: MethodDeclaration, programCounter: Int, method: MethodReference)
        extends Instr[invokespecial]
{

    val mnemonic = "invokespecial"

    val exceptions = Nil

}
