package sae
package bytecode

import sae.DefaultLazyView
import sae.bytecode.model._
import sae.reader._
import sae.syntax.RelationalAlgebraSyntax._
import sae.operators.BagProjection
import sae.bytecode.transform._
import de.tud.cs.st.bat._

trait BytecodeDatabase {

    // TODO check whether classfiles and classfile methods can be declared 
    // as views in combination with a classfile_source(Class, File) table
    // and how this affects performance
    val classfiles : LazyView[ObjectType] = new DefaultLazyView[ObjectType] {}

    val classfile_methods : LazyView[Method] = new DefaultLazyView[Method] {}

    val classes : LazyView[ObjectType] = new DefaultLazyView[ObjectType] {}

    val methods : LazyView[Method] = new DefaultLazyView[Method] {}

    val instructions : LazyView[Instr] = new DefaultLazyView[Instr] {}

    lazy val method_calls = Π((_ : Instr) match {
        case Instr(
            declaringMethod,
            programCounter,
            _,
            InvokeParameters(_, callee)) =>
            MethodCall(declaringMethod, callee, programCounter)
    }) (σ((_ : Instr).operation == "invoke")(instructions))

    lazy val transformer = new Java6ToSAE(
        classfiles,
        classfile_methods,
        classes,
        methods,
        instructions
    )

    def addArchiveAsResource(name : String) : Unit =
        {
            val factory = new SAEFactFactory(transformer)
            val reader = new BytecodeReader(factory)
            val stream = this.getClass().getClassLoader().getResourceAsStream(name)
            reader.readArchive(stream)
        }

    def addArchiveAsFile(name : String) : Unit =
        {
            val factory = new SAEFactFactory(transformer)
            val reader = new BytecodeReader(factory)
            reader.readArchive(new java.io.File(name))
        }

    // TODO addAsStream
}