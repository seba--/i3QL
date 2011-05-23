package sae
package bytecode
package model

import de.tud.cs.st.bat.{Type, ObjectType}

/**
 * Instruction was already taken as a name in bat and I wanted to avoid 
 * name clashes in wildcard imports
 */
case class Instr(
    declaringMethod : Method,
    programCounter : Int,
    operation : String,
    parameters : AnyRef) // we allow arbitrary data for each instruction

// data for an invoke
case class InvokeParameters(kind : String, callee : Method)

case class TypeCast(from : Type, to : Type)

case class Push()