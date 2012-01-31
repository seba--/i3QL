package sae
package bytecode

import sae.DefaultLazyView
import sae.bytecode.model._
import dependencies._
import internal._
import sae.bytecode.model.instructions._
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.transform._
import de.tud.cs.st.bat._
import sae.reader.BytecodeReader
import java.io.{File, InputStream}

/**
 *  extends(Class1, Class2)
 *  implements(Class1, Class2)
 *  field_type(FieldReference, Class)
 *  parameter(MethodReference, Class)
 *  return_type(MethodReference, Class)
 *  write_field(MethodReference, FieldReference)
 *  read_field(MethodReference, FieldReference)
 *  calls(Method1, Method2)
 *  class_cast(MethodReference, Class)
 *  instanceof(MethodReference, Class)
 *  create(MethodReference, Class)
 *  create_class_array(MethodReference, Class)
 *  throw(MethodReference, Class)
 *  get_class(MethodReference, Class)
 *  annotation(Class|FieldReference|MethodReference, Class)
 *  parameter_annotation(MethodReference, Class)
 */
class BytecodeDatabase extends Database
{
    /**
     * BEWARE INITIALIZATION ORDER OF FIELDS (scala compiler will not warn you)
     */

    // TODO check whether declared_types and classfile methods can be declared as views in combination with a classfile_source(Class, File) table and how this affects performance

    val class_declarations = new DefaultLazyView[ClassDeclaration]

    lazy val declared_types: LazyView[ObjectType] = Π((_:ClassDeclaration).objectType)(class_declarations)

    val classfile_methods: LazyView[MethodReference] = new DefaultLazyView[MethodReference]

    val classfile_fields: LazyView[FieldReference] = new DefaultLazyView[FieldReference]

    val classes: LazyView[ObjectType] = new DefaultLazyView[ObjectType]

    val methods: LazyView[MethodReference] = new DefaultLazyView[MethodReference]

    val fields: LazyView[FieldReference] = new DefaultLazyView[FieldReference]

    val instructions: LazyView[Instr[_]] = new DefaultLazyView[Instr[_]]

    /**
     * Begin with individual relations (derived are lazy vals others are filled during bytecode reading)
     */

    val `extends` : LazyView[`extends`] = new DefaultLazyView[`extends`]

    val implements: LazyView[implements] = new DefaultLazyView[implements]

    lazy val subtypes: LazyView[(ObjectType, ObjectType)] = TC(
        `extends`.∪[Dependency[ObjectType, ObjectType], implements](implements)
    )((_: Dependency[ObjectType, ObjectType]).source, (_: Dependency[ObjectType, ObjectType]).target)


    // inner classes are added multiple times for each inner class, since the definition is repeated in the classfile
    // TODO these relations are currently external for performance measurements
    val internal_inner_classes: LazyView[unresolved_inner_class_entry] = new DefaultLazyView[unresolved_inner_class_entry]

    val internal_enclosing_methods: LazyView[unresolved_enclosing_method] = new DefaultLazyView[unresolved_enclosing_method]

    /*
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
    */


    lazy val internal_guaranteed_inner_classes: LazyView[inner_class] =
        δ(
            Π(
                // the directly encoded inner classes have their outer type set
                    (entry: unresolved_inner_class_entry) =>
                    new inner_class(
                        entry.outerClassType,
                        entry.innerClassType,
                        true,
                        if (entry.innerName eq null) {
                            None
                        } else {
                            Some(entry.innerName)
                        }
                    )
            )(
                σ((_: unresolved_inner_class_entry).outerClassType ne null)(internal_inner_classes)
            )
        )
    /*
     * Deduces inner classes only by looking at the inner classes attribute.
     * Taking enclosing methods into account is not feasible for older jars.
     */
    lazy val inner_classes: LazyView[inner_class] =
        internal_guaranteed_inner_classes ∪
                (
                        Π(
                            // the directly encoded inner classes have their outer type set
                                (entry: unresolved_inner_class_entry) =>
                                new inner_class(
                                    entry.declaringClass,
                                    entry.innerClassType,
                                    false,
                                    if (entry.innerName eq null) {
                                        None
                                    } else {
                                        Some(entry.innerName)
                                    }
                                )
                        )
                                (
                                // TODO this is a pragmatic solution that checks that the name if the inner type is longer than the name of the outer type, it passes all tests, and it seems that classes never mention inner_classes beyond one level which might be falsely identified by this test
                                σ((e: unresolved_inner_class_entry) => (e.outerClassType eq null) && (e.innerClassType.className.length() > e.declaringClass.className.length()))(
                                    internal_inner_classes
                                )
                        )
                )
    lazy val field_type: LazyView[field_type] = Π((f: FieldReference) => new field_type(f, f.fieldType))(classfile_fields)

    val parameter: LazyView[parameter] = new DefaultLazyView[parameter]

    lazy val return_type: LazyView[return_type] = Π((m: MethodReference) => new return_type(m, m.returnType))(classfile_methods)

    val exception_handlers = new DefaultLazyView[ExceptionHandler]()

    // exception handelers can have an undefined catchType. This is used to implement finally blocks
    lazy val handled_exceptions: LazyView[ExceptionHandler] = σ((_: ExceptionHandler).catchType != None)(
        exception_handlers
    )

    val thrown_exceptions: LazyView[throws] = new DefaultLazyView[throws]()

    lazy val write_field: LazyView[write_field] =
        (
                Π[Instr[_], write_field] {
                    case putfield(declaringMethod, _, field) => new write_field(declaringMethod, field, false)
                }(σ[putfield](instructions))
                ) ∪ (
                Π[Instr[_], write_field] {
                    case putstatic(declaringMethod, _, field) => new write_field(declaringMethod, field, true)
                }(σ[putstatic](instructions))
                )

    lazy val read_field: LazyView[read_field] =
        (
                Π[Instr[_], read_field] {
                    case getfield(declaringMethod, _, field) => new read_field(declaringMethod, field, false)
                }(σ[getfield](instructions))
                ) ∪ (
                Π[Instr[_], read_field] {
                    case getstatic(declaringMethod, _, field) => new read_field(declaringMethod, field, true)
                }(σ[getstatic](instructions))
                )

    lazy val invoke_interface: LazyView[invoke_interface] = Π(
        (_: Instr[_]) match {
            case invokeinterface(declaringMethod, pc, callee) => new invoke_interface(declaringMethod, callee)
        }
    )(σ[invokeinterface](instructions))

    lazy val invoke_special: LazyView[invoke_special] = Π(
        (_: Instr[_]) match {
            case invokespecial(declaringMethod, pc, callee) => new invoke_special(declaringMethod, callee)
        }
    )(σ[invokespecial](instructions))

    lazy val invoke_virtual: LazyView[invoke_virtual] = Π(
        (_: Instr[_]) match {
            case invokevirtual(declaringMethod, pc, callee) => new invoke_virtual(declaringMethod, callee)
        }
    )(σ[invokevirtual](instructions))

    lazy val invoke_static: LazyView[invoke_static] = Π(
        (_: Instr[_]) match {
            case invokestatic(declaringMethod, pc, callee) => new invoke_static(declaringMethod, callee)
        }
    )(σ[invokestatic](instructions))

    /*
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
    */
    lazy val calls: LazyView[calls] = invoke_interface.∪[calls, calls](
        invoke_special.∪[calls, calls](
            invoke_virtual.∪[calls, invoke_static](
                invoke_static
            )
        )
    )


    // TODO array references to primitive arrays are exempted, is this okay
    lazy val class_cast: LazyView[class_cast] =
        Π[Instr[_], class_cast] {
            case checkcast(declaringMethod, _, to) => new class_cast(declaringMethod, to)
        }(
            σ(
                (_: Instr[_]) match {
                    case checkcast(_, _, ObjectType(_)) => true
                    case checkcast(_, _, ArrayType(ObjectType(_))) => true
                    case _ => false
                }
            )(instructions)
        )

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
            σ(
                (_: Instr[_]) match {
                    case newarray(_, _, ObjectType(_)) => true
                    case _ => false
                }
            )(instructions)
        )

    lazy val dependency: LazyView[Dependency[AnyRef, AnyRef]] =
        `extends`.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
            implements.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                inner_classes.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                    field_type.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                        parameter.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                            return_type.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                                Π(
                                        (handler: ExceptionHandler) =>
                                        handled_exception(
                                            handler.declaringMethod,
                                            handler.catchType.get
                                        ) // the handled_exceptions relation is prefiltered, so we do not run into trouble with catchType == None here
                                )(handled_exceptions).∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                                    thrown_exceptions.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                                        write_field.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                                            read_field.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                                                calls.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                                                    class_cast.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                                                        create.∪[Dependency[AnyRef, AnyRef], Dependency[AnyRef, AnyRef]](
                                                            create_class_array.∪[Dependency[AnyRef, AnyRef], sae.bytecode.model.dependencies.instanceof](
                                                                instanceof
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )


    lazy val baseViews: List[LazyView[_]] = List(
        declared_types,
        classfile_methods,
        classfile_fields,
        classes,
        methods,
        fields,
        instructions,
        `extends`,
        implements,
        parameter,
        exception_handlers,
        internal_inner_classes,
        internal_enclosing_methods
    )

    lazy val derivedViews: List[LazyView[_]] = List(
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
        create_class_array,
        handled_exceptions
    )


    private def classAdder = new Java6ClassTransformer(
        class_declarations.element_added,
        classfile_methods.element_added,
        classfile_fields.element_added,
        classes.element_added,
        methods.element_added,
        fields.element_added,
        instructions.element_added,
        `extends`.element_added,
        implements.element_added,
        parameter.element_added,
        exception_handlers.element_added,
        thrown_exceptions.element_added,
        internal_inner_classes.element_added,
        internal_enclosing_methods.element_added
    )


    private def classRemover = new Java6ClassTransformer(
        class_declarations.element_removed,
        classfile_methods.element_removed,
        classfile_fields.element_removed,
        classes.element_removed,
        methods.element_removed,
        fields.element_removed,
        instructions.element_removed,
        `extends`.element_removed,
        implements.element_removed,
        parameter.element_removed,
        exception_handlers.element_removed,
        thrown_exceptions.element_removed,
        internal_inner_classes.element_removed,
        internal_enclosing_methods.element_removed
    )


    /**
     * Read a stream as a jar file and return the appropriate transformer
     */
    def transformerForArchiveStream(stream: InputStream) =
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        reader.readArchive(stream)
        transformer
    }

    /**
     * Read from a list of .jar file streams and return the appropriate transformer
     */
    def transformerForArchiveStreams(streams: Seq[InputStream]) =
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        streams.foreach(reader.readArchive(_))
        transformer
    }


    /**
     * Read from a list of .jar file streams and return the appropriate transformer
     */
    def transformerForArchiveResources(names: Seq[String]) =
    {
        transformerForArchiveStreams(
            names.map(this.getClass.getClassLoader.getResourceAsStream(_))
        )
    }

    /**
     * Convenience method that opens a stream from a resource in the class path
     */
    def transformerForArchiveResource(name: String): Java6ClassTransformer =
    {
        val stream = this.getClass.getClassLoader.getResourceAsStream(name)
        transformerForArchiveStream(stream)
    }

    /**
     * Read a stream as a single .class file and return the appropriate transformer
     */
    def transformerForClassfileStream(stream: InputStream) =
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        reader.readClassFile(stream)
        transformer
    }

    /**
     * Read from a list of .class file streams and return the appropriate transformer
     */
    def transformerForClassfileStreams(streams: Seq[InputStream]) =
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        streams.foreach(reader.readClassFile(_))
        transformer
    }

    def transformerForClassfileResources(names: Seq[String]): Java6ClassTransformer =
    {
        val streams = names.map(this.getClass.getClassLoader.getResourceAsStream(_))
        transformerForClassfileStreams(streams)
    }

    /**
     * Convenience method that opens a stream from a given loaded class
     */
    def transformerForClass[T](clazz: Class[T]): Java6ClassTransformer =
    {
        val name = clazz.getName.replace(".", "/") + ".class"
        val stream = clazz.getClassLoader.getResourceAsStream(name)
        transformerForClassfileStream(stream)
    }

    /**
     * Convenience method that opens a stream from a given loaded class
     */
    def transformerForClasses(classes: Array[Class[_]]): Java6ClassTransformer =
    {
        val streams = classes.map(
                (clazz: Class[_]) => (clazz.getClassLoader.getResourceAsStream(
                clazz.getName.replace(
                    ".",
                    "/"
                ) + ".class"
            ))
        )
        transformerForClassfileStreams(streams)
    }


    /**
     * Convenience method that opens a stream from a resource in the class path
     */
    def addArchiveAsResource(name: String)
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        val stream = this.getClass.getClassLoader.getResourceAsStream(name)
        reader.readArchive(stream)
        transformer.processAllFacts()
    }


    /**
     * Convenience method that opens a stream from a file in the file system
     */
    def addArchiveAsFile(name: String)
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        reader.readArchive(new java.io.File(name))
        transformer.processAllFacts()
    }

    /**
     * Read a jar archive from the stream.
     * The underlying data is assumed to be in zip (jar) format
     */
    def addArchive(stream: java.io.InputStream)
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        reader.readArchive(stream)
        transformer.processAllFacts()
    }

    def removeArchive(stream: java.io.InputStream)
    {
        val transformer = classRemover
        val reader = new BytecodeReader(transformer)
        reader.readArchive(stream)
        transformer.processAllFacts()
    }

    def getAddClassFileFunction: (File) => Unit =
    {

        //addReader.readClassFile(file)
        val f = (x: File) => {
            val transformer = classAdder
            val addReader = new BytecodeReader(transformer)
            addReader.readClassFile(x)
            transformer.processAllFacts()
        }
        f
    }

    def getRemoveClassFileFunction: (File) => Unit =
    {

        val f = (x: File) => {
            val transformer = classRemover
            val removeReader = new BytecodeReader(transformer)
            removeReader.readClassFile(x)
            transformer.processAllFacts()
        }
        f
    }

    def addClassFile (stream: java.io.InputStream)
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        reader.readClassFile(stream)
        transformer.processAllFacts()
    }

    def removeClassFile(stream: java.io.InputStream)
    {
        val transformer = classRemover
        val reader = new BytecodeReader(transformer)
        reader.readClassFile(stream)
        transformer.processAllFacts()
    }

}
