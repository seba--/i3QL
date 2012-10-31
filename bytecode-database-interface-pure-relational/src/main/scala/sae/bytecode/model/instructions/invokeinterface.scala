package sae.bytecode.model
package instructions

import sae.bytecode.model.MethodDeclaration

case class invokeinterface(declaringMethod: MethodDeclaration, programCounter: Int, method: MethodReference)
        extends Instr[invokeinterface]
{

    val mnemonic = "invokeinterface"

    val exceptions = Nil

}
