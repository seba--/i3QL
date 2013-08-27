package sae.bytecode.model
package instructions

import sae.bytecode.model.FieldReference

case class getfield(declaringMethod: MethodDeclaration, programCounter: Int, field: FieldReference)
        extends Instr[getfield]
{

    val mnemonic = "getfield"

    val exceptions = Nil

}
