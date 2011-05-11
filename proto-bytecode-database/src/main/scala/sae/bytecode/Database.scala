package sae
package bytecode

import sae.DefaultLazyView
import sae.bytecode.model._
import sae.reader._
import sae.bytecode.transform.SAEFactFactory

trait BytecodeDatabase
{
    val classfiles : LazyView[ClassFile] = new DefaultLazyView[ClassFile] {} 
    
    val classfile_methods  : LazyView[Method] = new DefaultLazyView[Method] {}
    
    def addArchiveAsResource(name : String) : Unit = 
    {
        val factory = new SAEFactFactory(this.classfiles, this.classfile_methods)
        val reader = new BytecodeReader(factory)
        val stream = this.getClass().getClassLoader().getResourceAsStream(name)
        reader.readArchive(stream)
    }

    def addArchiveAsFile(name : String) : Unit = 
    {
        val factory = new SAEFactFactory(this.classfiles, this.classfile_methods)
        val reader = new BytecodeReader(factory)
        reader.readArchive(new java.io.File(name))
    }
    
    // TODO addAsStream
}