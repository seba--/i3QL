package sae
package bytecode

import sae.DefaultLazyView
import sae.bytecode.model._
import sae.reader._
import sae.syntax.RelationalAlgebraSyntax._
import sae.operators.BagProjection
import sae.bytecode.transform._
import de.tud.cs.st.bat._

class BytecodeDatabase {

    // TODO check whether classfiles and classfile methods can be declared 
    // as views in combination with a classfile_source(Class, File) table
    // and how this affects performance
    val classfiles : LazyView[ObjectType] = new DefaultLazyView[ObjectType] {}

    val classfile_methods : LazyView[Method] = new DefaultLazyView[Method] {}

    val classes : LazyView[ObjectType] = new DefaultLazyView[ObjectType] {}

    val methods : LazyView[Method] = new DefaultLazyView[Method] {}

    val instructions : LazyView[Instr] = new DefaultLazyView[Instr] {}

    lazy val method_calls : LazyView[MethodCall] = Π((_ : Instr) match {
        case Instr(
            declaringMethod,
            programCounter,
            _,
            InvokeParameters(_, callee)) =>
            MethodCall(declaringMethod, callee, programCounter)
    })(σ((_ : Instr).operation == "invoke")(instructions))

    
    lazy val transformer = new Java6ToSAE(
        classfiles,
        classfile_methods,
        classes,
        methods,
        instructions
    )

    /**
     * Convenience method that opens a stream from a resource in the class path
     */
    def addArchiveAsResource(name : String) : Unit =
        {
            val factory = new SAEFactFactory(transformer)
            val reader = new BytecodeReader(factory)
            val stream = this.getClass().getClassLoader().getResourceAsStream(name)
            reader.readArchive(stream)
        }

    /**
     * Convenience method that opens a stream from a file in the file system
     */
    def addArchiveAsFile(name : String) : Unit =
        {
            val factory = new SAEFactFactory(transformer)
            val reader = new BytecodeReader(factory)
            reader.readArchive(new java.io.File(name))
        }

    /**
     * Read a jar archive from the stream.
     * The underlying data is assumed to be in zip (jar) format
     */
    def addArchiveStream(stream : java.io.InputStream) : Unit =
        {
            val factory = new SAEFactFactory(transformer)
            val reader = new BytecodeReader(factory)
            reader.readArchive(stream)
        }

}