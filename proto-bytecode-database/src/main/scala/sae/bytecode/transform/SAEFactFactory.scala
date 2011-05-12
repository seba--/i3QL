package sae
package bytecode
package transform

import sae.bytecode.model._
import sae.reader.BytecodeFactFactory

class SAEFactFactory(classes : Observable[ClassFile], methods : Observable[Method]) 
	extends BytecodeFactFactory
{
    val transformer = new Java6ToSAE(classes, methods)
    
	def addClassFile (cf:de.tud.cs.st.bat.ClassFile) : Unit =
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
