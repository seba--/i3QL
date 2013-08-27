package sae.java.instructions

import sae.java.members.Method

/**
 * Instruction was already taken as a name in bat and I wanted to avoid 
 * name clashes in wildcard imports
 */
trait Instruction[T] {
    val declaringMethod: Method

    val programCounter: Int

    type Kind = T
}