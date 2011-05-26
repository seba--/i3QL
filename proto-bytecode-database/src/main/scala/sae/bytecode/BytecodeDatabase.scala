package sae
package bytecode

import sae.DefaultLazyView
import sae.bytecode.model._
import sae.bytecode.model.instructions._
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

    lazy val field_type: LazyView[field_type] = Π((f: Field) => new field_type(f, f.fieldType))(classfile_fields)

    // Π( (f:Field) => new field_type(f, f.fieldType.asObjectType) )( σ((_: Field).fieldType.isObjectType)(fields) )

    val parameter: LazyView[parameter] = new DefaultLazyView[parameter]

    lazy val return_type: LazyView[return_type] = Π((m: Method) => new return_type(m, m.returnType))(classfile_methods)

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
        Π[Instr[_], sae.bytecode.model.dependencies.instanceof]{
            case sae.bytecode.model.instructions.instanceof(declaringMethod, _, typ) =>
                sae.bytecode.model.dependencies.instanceof(declaringMethod, typ)
        }(σ[sae.bytecode.model.instructions.instanceof](instructions))


    lazy val create: LazyView[create] =
        Π[Instr[_], create]{
            case `new`(declaringMethod, _, typ) => new create(declaringMethod, typ)
        }(σ[`new`](instructions))

    lazy val create_class_array: LazyView[create_class_array] =
        Π[Instr[_], create_class_array]{
            case newarray(declaringMethod, _, typ @ ObjectType(_)) => new create_class_array(declaringMethod, typ)
        }(
            σ( (_: Instr[_]) match {
                    case newarray(_, _, ObjectType(_)) => true
                    case _ => false
                }
            )(instructions)
        )


    lazy val transformer = new Java6ToSAE(
        classfiles,
        classfile_methods,
        classfile_fields,
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
    def addArchiveAsResource(name: String): Unit =
    {
        val factory = new SAEFactFactory(transformer)
        val reader = new BytecodeReader(factory)
        val stream = this.getClass().getClassLoader().getResourceAsStream(name)
        reader.readArchive(stream)
    }

    /**
     * Convenience method that opens a stream from a file in the file system
     */
    def addArchiveAsFile(name: String): Unit =
    {
        val factory = new SAEFactFactory(transformer)
        val reader = new BytecodeReader(factory)
        reader.readArchive(new java.io.File(name))
    }

    /**
     * Read a jar archive from the stream.
     * The underlying data is assumed to be in zip (jar) format
     */
    def addArchiveStream(stream: java.io.InputStream): Unit =
    {
        val factory = new SAEFactFactory(transformer)
        val reader = new BytecodeReader(factory)
        reader.readArchive(stream)
    }

}