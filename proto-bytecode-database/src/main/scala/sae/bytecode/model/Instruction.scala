package sae
package bytecode
package model

/**
 * Instruction was already taken as a name in bat and I wanted to avoid 
 * name clashes in wildcard imports
 */
case class Instr(
    declaringMethod : Method,
    programCounter : Int,
    operation : String,
    parameters : InstructionParameters)

trait InstructionParameters

case class InvokeParameters(kind : String, callee : Method) extends InstructionParameters 