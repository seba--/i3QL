package sae
package bytecode
package transform

import sae.bytecode.model._
import sae.reader.BytecodeFactFactory

class SAEFactFactory(
    val transformer : Java6ToSAE)
        extends BytecodeFactFactory {

    def addClassFile(cf : de.tud.cs.st.bat.ClassFile) : Unit =
        {
            transformer.transform(cf)
        }

    def addAllFacts : Unit =
        {
            // do nothing we need no extra processing of added classfiles 
            // but transform the directly
            // here we could schedule parallelization
        }

}
