package sae
package bytecode

import sae.DefaultLazyView
import sae.bytecode.model._
import dependencies._
import internal.unresolved_enclosing_method
import sae.bytecode.model.instructions._
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.transform._
import de.tud.cs.st.bat._
import sae.reader.BytecodeReader
import java.io.{File, InputStream}

/**
 *  extends(Class1, Class2)
 *  implements(Class1, Class2)
 *  field_type(Field, Class)
 *  parameter(Method, Class)
 *  return_type(Method, Class)
 *  write_field(Method, Field)
 *  read_field(Method, Field)
 *  calls(Method1, Method2)
 *  class_cast(Method, Class)
 *  instanceof(Method, Class)
 *  create(Method, Class)
 *  create_class_array(Method, Class)
 *  throw(Method, Class)
 *  get_class(Method, Class)
 *  annotation(Class|Field|Method, Class)
 *  parameter_annotation(Method, Class)
 */
class BytecodeDatabase
{
    /**
     * BEWARE INITIALIZATION ORDER OF FIELDS (scala compiler will not warn you)
     */

    // TODO check whether classfiles and classfile methods can be declared 
    // as views in combination with a classfile_source(Class, File) table
    // and how this affects performance
    val classfiles: LazyView[ObjectType] = new DefaultLazyView[ObjectType]

    val classfile_methods: LazyView[Method] = new DefaultLazyView[Method]

    val classfile_fields: LazyView[Field] = new DefaultLazyView[Field]

    val classes: LazyView[ObjectType] = new DefaultLazyView[ObjectType]

    val methods: LazyView[Method] = new DefaultLazyView[Method]

    val fields: LazyView[Field] = new DefaultLazyView[Field]

    val instructions: LazyView[Instr[_]] = new DefaultLazyView[Instr[_]]

    /**
     * Begin with individual relations (derived are lazy vals others are filled during bytecode reading)
     */

    val `extends` : LazyView[`extends`] = new DefaultLazyView[`extends`]

    val implements: LazyView[implements] = new DefaultLazyView[implements]

    lazy val subtypes : LazyView[(ObjectType, ObjectType)] = TC(
        `extends`.∪[Dependency[ObjectType, ObjectType], implements] (implements)
    )( (_:Dependency[ObjectType, ObjectType]).source, (_:Dependency[ObjectType, ObjectType]).target )


    // inner classes are added multiple times for each inner class, since the definition is repeated in the classfile
    // TODO these relations are currently external for performance measurements
    val internal_inner_classes: LazyView[InnerClassesEntry] = new DefaultLazyView[InnerClassesEntry]

    val internal_enclosing_methods: LazyView[unresolved_enclosing_method] = new DefaultLazyView[unresolved_enclosing_method]

    lazy val inner_classes: LazyView[inner_class] =
        Π( // the directly encoded inner classes have their outer type set
                (entry : InnerClassesEntry) =>
                    new inner_class(
                        entry.outerClassType,
                        entry.innerClassType,
                        true,
                        if(entry.innerName eq null) { None } else {Some(entry.innerName)})
        ) ( σ(  (_:InnerClassesEntry).outerClassType ne null )( δ (internal_inner_classes) ) ) ∪
        (
            ((
                // all inner classes without explicit outer class
                σ(  (_:InnerClassesEntry).outerClassType eq null )( δ (internal_inner_classes) ),
                (entry : InnerClassesEntry) => entry.innerClassType
            ) ⋈ ( // for complete data, i.e. is it an inner class and is it named we need a join
                // outer determined by enclosing method
                (_ : unresolved_enclosing_method).innerClass,
                    internal_enclosing_methods
            )) {
                    (entry : InnerClassesEntry, enc : unresolved_enclosing_method) =>
                        new inner_class(
                            enc.declaringClass,
                            enc.innerClass,
                            false,
                            if(entry.innerName eq null) {None} else {Some(entry.innerName)}
                        )
            }
        )

    lazy val field_type: LazyView[field_type] = Π((f: Field) => new field_type(f, f.fieldType))(classfile_fields)

    val parameter: LazyView[parameter] = new DefaultLazyView[parameter]

    lazy val return_type: LazyView[return_type] = Π((m: Method) => new return_type(m, m.returnType))(classfile_methods)

    val exception_table_entries = new DefaultLazyView[ExceptionHandler]()

    lazy val throws : LazyView[throws] = δ(
        Π( (e:ExceptionHandler) => new throws(e.declaringMethod, e.catchType.get)) (
            σ( (_:ExceptionHandler).catchType != None )(exception_table_entries) )
        )

    lazy val write_field: LazyView[write_field] =
        (
                Π[Instr[_], write_field] {
                    case putfield(declaringMethod, _, field) => new write_field(declaringMethod, field)
                }(σ[putfield](instructions))
                ) ∪ (
                Π[Instr[_], write_field] {
                    case putstatic(declaringMethod, _, field) => new write_field(declaringMethod, field)
                }(σ[putstatic](instructions))
                )

    lazy val read_field: LazyView[read_field] =
        (
                Π[Instr[_], read_field] {
                    case getfield(declaringMethod, _, field) => new read_field(declaringMethod, field)
                }(σ[getfield](instructions))
                ) ∪ (
                Π[Instr[_], read_field] {
                    case getstatic(declaringMethod, _, field) => new read_field(declaringMethod, field)
                }(σ[getstatic](instructions))
                )

    lazy val invoke_interface: LazyView[invoke_interface] = Π((_: Instr[_]) match {
        case invokeinterface(declaringMethod, pc, callee) => new invoke_interface(declaringMethod, callee)
    })(σ[invokeinterface](instructions))

    lazy val calls: LazyView[calls] = Π((_: Instr[_]) match {
        case invokeinterface(declaringMethod, pc, callee) => new calls(declaringMethod, callee)
        case invokespecial(declaringMethod, pc, callee) => new calls(declaringMethod, callee)
        case invokestatic(declaringMethod, pc, callee) => new calls(declaringMethod, callee)
        case invokevirtual(declaringMethod, pc, callee) => new calls(declaringMethod, callee)
    }
    )(σ((_: Instr[_]) match {
        case invokeinterface(_, _, _) => true
        case invokespecial(_, _, _) => true
        case invokestatic(_, _, _) => true
        case invokevirtual(_, _, _) => true
        case _ => false
    }
    )(instructions))


    // TODO array references to primitive arrays are exempted, is this okay
    lazy val class_cast: LazyView[class_cast] =
        Π[Instr[_], class_cast] {
            case checkcast(declaringMethod, _, to) => new class_cast(declaringMethod, to)
        }(
            σ((_: Instr[_]) match {
                case checkcast(_, _, ObjectType(_)) => true
                case checkcast(_, _, ArrayType(ObjectType(_))) => true
                case _ => false
            }
            )(instructions))

    // TODO can we find a better name for the dependency than instanceof
    lazy val instanceof: LazyView[sae.bytecode.model.dependencies.instanceof] =
        Π[Instr[_], sae.bytecode.model.dependencies.instanceof] {
            case sae.bytecode.model.instructions.instanceof(declaringMethod, _, typ) =>
                sae.bytecode.model.dependencies.instanceof(declaringMethod, typ)
        }(σ[sae.bytecode.model.instructions.instanceof](instructions))


    lazy val create: LazyView[create] =
        Π[Instr[_], create] {
            case `new`(declaringMethod, _, typ) => new create(declaringMethod, typ)
        }(σ[`new`](instructions))

    lazy val create_class_array: LazyView[create_class_array] =
        Π[Instr[_], create_class_array] {
            case newarray(declaringMethod, _, typ@ObjectType(_)) => new create_class_array(declaringMethod, typ)
        }(
            σ((_: Instr[_]) match {
                case newarray(_, _, ObjectType(_)) => true
                case _ => false
            }
            )(instructions)
        )


    lazy val baseViews : List[LazyView[_]] = List(
        classfiles,
        classfile_methods,
        classfile_fields,
        classes,
        methods,
        fields,
        instructions,
        `extends`,
        implements,
        parameter,
        exception_table_entries,
        internal_inner_classes,
        internal_enclosing_methods
    )

    lazy val derivedViews : List[LazyView[_]] = List(
        subtypes,
        inner_classes,
        field_type,
        return_type,
        write_field,
        read_field,
        invoke_interface,
        calls,
        class_cast,
        instanceof,
        create,
        create_class_array
    )


    private def classAdder = new Java6ClassTransformer(
        classfiles.element_added,
        classfile_methods.element_added,
        classfile_fields.element_added,
        classes.element_added,
        methods.element_added,
        fields.element_added,
        instructions.element_added,
        `extends`.element_added,
        implements.element_added,
        parameter.element_added,
        exception_table_entries.element_added,
        internal_inner_classes.element_added,
        internal_enclosing_methods.element_added
    )


    private def classRemover = new Java6ClassTransformer(
        classfiles.element_removed,
        classfile_methods.element_removed,
        classfile_fields.element_removed,
        classes.element_removed,
        methods.element_removed,
        fields.element_removed,
        instructions.element_removed,
        `extends`.element_removed,
        implements.element_removed,
        parameter.element_removed,
        exception_table_entries.element_removed,
        internal_inner_classes.element_removed,
        internal_enclosing_methods.element_removed
    )


    /**
     * Read a stream as a jar file and return the appropriate transformer
     */
    def transformerForArchiveStream(stream : InputStream) = {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        reader.readArchive(stream)
        transformer
    }

    /**
     * Convenience method that opens a stream from a resource in the class path
     */
    def transformerForArchiveResource(name: String) : Java6ClassTransformer = {
        val stream = this.getClass.getClassLoader.getResourceAsStream(name)
        transformerForArchiveStream(stream)
    }

    /**
     * Read a stream as a single .class file and return the appropriate transformer
     */
    def transformerForClassfileStream(stream : InputStream) = {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        reader.readClassFile(stream)
        transformer
    }

    /**
     * Read from a list of .class file streams and return the appropriate transformer
     */
    def transformerForClassfileStreams(streams : Seq[InputStream]) = {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        streams.foreach(reader.readClassFile(_))
        transformer
    }

    def transformerForClassfileResources(names: Seq[String]) : Java6ClassTransformer = {
        val streams = names.map( this.getClass.getClassLoader.getResourceAsStream(_) )
        transformerForClassfileStreams(streams)
    }

    /**
     * Convenience method that opens a stream from a given loaded class
     */
    def transformerForClass[T](clazz: Class[T]) : Java6ClassTransformer = {
        val name = clazz.getName.replace(".", "/") + ".class"
        val stream = clazz.getClassLoader.getResourceAsStream(name)
        transformerForClassfileStream(stream)
    }

    /**
     * Convenience method that opens a stream from a given loaded class
     */
    def transformerForClasses(classes: Array[Class[_]]) : Java6ClassTransformer = {
        val streams = classes.map( (clazz:Class[_]) => (clazz.getClassLoader.getResourceAsStream(clazz.getName.replace(".", "/") + ".class")) )
        transformerForClassfileStreams(streams)
    }


    /**
     * Convenience method that opens a stream from a resource in the class path
     */
    def addArchiveAsResource(name: String) {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        val stream = this.getClass.getClassLoader.getResourceAsStream(name)
        reader.readArchive(stream)
        transformer.processAllFacts()
    }



    /**
     * Convenience method that opens a stream from a file in the file system
     */
    def addArchiveAsFile(name: String) {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        reader.readArchive(new java.io.File(name))
        transformer.processAllFacts()
    }

    /**
     * Read a jar archive from the stream.
     * The underlying data is assumed to be in zip (jar) format
     */
    def addArchiveStream(stream: java.io.InputStream) {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        reader.readArchive(stream)
        transformer.processAllFacts()
    }

    def getAddClassFileFunction: (File) => Unit = {
        
        //addReader.readClassFile(file)
        val f = (x : File) => {
            val transformer = classAdder
            val addReader = new BytecodeReader(transformer)
            addReader.readClassFile(x)
            transformer.processAllFacts()
        }
        f
    }
    
    def getRemoveClassFileFunction: (File) => Unit = {
        
        val f = (x : File) => {
            val transformer = classRemover
            val removeReader = new BytecodeReader(transformer)
            removeReader.readClassFile(x)
            transformer.processAllFacts()
        }
        f
    }
  

}
