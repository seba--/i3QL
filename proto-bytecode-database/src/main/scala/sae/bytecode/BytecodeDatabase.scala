package sae
package bytecode

import sae.DefaultLazyView
import sae.bytecode.model._
import dependencies._
import sae.reader._
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.transform._
import de.tud.cs.st.bat._
/**
 *  extends(Class1, Class2)
 *  implements(Class1, Class2)
 *  field_type(Field, Class)
 *  parameter(Method, Class)
 *  return_type(Method, Class)
 *  write_field(Method, Field)
 *  write_static_field(Method, Field)
 *  read_field(Method, Field)
 *  read_static_field(Method, Field)
 *  invoke_virtual(Method1, Method2)
 *  invoke_special(Method1, Method2)
 *  invoke_interface(Method1, Method2)
 *  invoke_static(Method1, Method2)
 *  cast(Method, Class)
 *  instanceof(Method, Class)
 *  create(Method, Class)
 *  create_class_array(Method, Class)
 *  throw(Method, Class)
 *  get_class(Method, Class)
 *  annotation(Class|Field|Method, Class)
 *  parameter_annotation(Method, Class)
 */
class BytecodeDatabase {

    // TODO check whether classfiles and classfile methods can be declared 
    // as views in combination with a classfile_source(Class, File) table
    // and how this affects performance
    val classfiles: LazyView[ObjectType] = new DefaultLazyView[ObjectType] {}

    val classfile_methods: LazyView[Method] = new DefaultLazyView[Method] {}

    val classes: LazyView[ObjectType] = new DefaultLazyView[ObjectType] {}

    val methods: LazyView[Method] = new DefaultLazyView[Method] {}

    val fields: LazyView[Field] = new DefaultLazyView[Field] {}

    val instructions: LazyView[Instr] = new DefaultLazyView[Instr] {}

    /**
     * Begin with individual relations (derived are lazy vals others are filled during bytecode reading)
     */

    val `extends` : LazyView[`extends`] = new DefaultLazyView[`extends`]

    val implements: LazyView[implements] = new DefaultLazyView[implements]

    lazy val field_type : LazyView[field_type] =
        Π( (f:Field) => new field_type(f, f.fieldType.asObjectType) )( σ((_: Field).fieldType.isObjectType)(fields) )

    val parameter: LazyView[parameter] = new DefaultLazyView[parameter]

    lazy val return_type: LazyView[return_type] = Π( (m:Method) => new return_type(m, m.returnType) ) (methods)

    lazy val write_field: LazyView[write_field] = new DefaultLazyView[write_field]

    lazy val method_calls: LazyView[MethodCall] = Π((_: Instr) match {
        case Instr(
        declaringMethod,
        programCounter,
        _,
        InvokeParameters(_, callee)) =>
            MethodCall(declaringMethod, callee, programCounter)
    })(σ((_: Instr).operation == "invoke")(instructions))


    //lazy val uses : LazyView[Uses[_,_]] =

    lazy val transformer = new Java6ToSAE(
        classfiles,
        classfile_methods,
        classes,
        methods,
        fields,
        instructions,
        `extends`,
        implements,
        parameter
    )

    /**
     * Convenience method that opens a stream from a resource in the class path
     */
    def addArchiveAsResource(name: String): Unit = {
        val factory = new SAEFactFactory(transformer)
        val reader = new BytecodeReader(factory)
        val stream = this.getClass().getClassLoader().getResourceAsStream(name)
        reader.readArchive(stream)
    }

    /**
     * Convenience method that opens a stream from a file in the file system
     */
    def addArchiveAsFile(name: String): Unit = {
        val factory = new SAEFactFactory(transformer)
        val reader = new BytecodeReader(factory)
        reader.readArchive(new java.io.File(name))
    }

    /**
     * Read a jar archive from the stream.
     * The underlying data is assumed to be in zip (jar) format
     */
    def addArchiveStream(stream: java.io.InputStream): Unit = {
        val factory = new SAEFactFactory(transformer)
        val reader = new BytecodeReader(factory)
        reader.readArchive(stream)
    }

}