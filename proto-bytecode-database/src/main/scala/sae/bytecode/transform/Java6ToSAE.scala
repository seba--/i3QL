package sae
package bytecode
package transform

import sae.Observable
import sae.bytecode.model.ClassFile
import sae.bytecode.model.Method
import de.tud.cs.st.bat._

class Java6ToSAE(
        classes : Observable[ClassFile],
        methods : Observable[Method]) {

    /**
     *
     */
    def transform(classFile : de.tud.cs.st.bat.ClassFile) : Unit =
        {
            // these classes are always unique, no check is performed
    		val c = ClassFile(classFile.thisClass.packageName, classFile.thisClass.simpleName)
            classes.element_added(c)

            classFile.methods.foreach(transform(c,_))
        }

    /**
     *
     */
    def transform(declaringClass : ClassFile, method_info : Method_Info) : Unit =
        {
    		val method = Method(declaringClass, method_info.name, method_info.descriptor.parameterTypes, method_info.descriptor.returnType)
    		methods.element_added(method);
    		
        }
    
    //transform (code_attribute) 
    
    
    //transform(InvokeInstruction)
}