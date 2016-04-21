package sae.bytecode.asm

import org.objectweb.asm.Type

/**
 *
 * @author Ralf Mitschke
 *
 */
trait ASMTypeOrdering
{

    implicit def typeOrdering (): Ordering[Type] =
        new Ordering[Type]
        {
            def compare (x: Type, y: Type): Int =
                x.toString.compareTo (y.toString)
        }
}
