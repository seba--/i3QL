package sae.java

import sae.java.members._
import model.internal.unresolved_enclosing_method
import sae.java.instructions._
import de.tud.cs.st.bat._
import de.tud.cs.st.bat.instructions._
import sae.reader.BytecodeFactProcessor
import types.ObjectType

/**
 * Transform a classfile to the SAE representation
 * This implementation delivers the following guarantees
 * Every class and method is distinct on the object level.
 * Thus no two objects with the same values exist.
 * Types are currently NOT distinct
 *
 * A transformer receives a set of methods that are called on each new element.
 * Thus the transformer can be configured to add/delete or update elements or perform
 * altogether different operations
 *
 * There is a tradeoff between having distinct classes/methods and
 * looking concrete objects up during construction.
 * Each lookup costs construction time < vs. > each object costs memory
 * Furthermore subsequent queries could make object referential equality checks for better performance.
 */
class Java6ClassTransformer(
                        process_class: (de.tud.cs.st.bat.ObjectType, Option[de.tud.cs.st.bat.ClassFileCategory.ClassFileCategory], Boolean) => ObjectType,
                        process_method: (de.tud.cs.st.bat.ReferenceType, String, Seq[de.tud.cs.st.bat.Type], de.tud.cs.st.bat.Type) => Method,
                        process_field: (de.tud.cs.st.bat.ObjectType, String, FieldType, Boolean) => Field,
                        process_extends : (ObjectType, de.tud.cs.st.bat.ObjectType) => Unit,
                        process_implements: (ObjectType, de.tud.cs.st.bat.ObjectType) => Unit
                        /*,
                        process_instruction: Instruction[_] => Unit,
                        process_exception_handler: ExceptionHandler => Unit,
                        process_thrown_exception: throws => Unit,
                        process_inner_class_entry : InnerClassesEntry => Unit,
                        process_enclosing_method : unresolved_enclosing_method => Unit
                        */
)
        extends TransformInstruction[Unit, Method] with
                BytecodeFactProcessor
{


    private var list : List[ClassFile]= List()

    def processClassFile(cf : de.tud.cs.st.bat.ClassFile) {
        list = cf :: list
    }

    def processAllFacts() {
        // do nothing we need no extra processing of added process_classfile
        // but transform the directly
        // here we could schedule parallelization

        list.foreach(transform)

    }

    // TODO: ideally we would search for a view on all process_class and reuse that here

    /**
     * Conversions from constant value to a type
     */
    private def constant_value_function(constantValue : ConstantValue[_]) : () => _ =
        constantValue.constant_Pool_EntryType match
        {
            // Note : hand coded magic numbers elicit a table switch
            case /* CONSTANT_Utf8 */ 	1 => () => constantValue.toUTF8
            case /* CONSTANT_Integer */ 3 => () => constantValue.toInt
            case /* CONSTANT_Float */ 	4 => () => constantValue.toFloat
            case /* CONSTANT_Long */ 	5 => () => constantValue.toLong
            case /* CONSTANT_Double */ 	6 => () => constantValue.toDouble
            case /* CONSTANT_Class */ 	7 => () => constantValue.toClass
        }


	private def lastInstructionIndex( bytecodeMap : Array[Int] ) : Int = {
		var i = bytecodeMap.length - 1;
		while( i > 0 ) {
			if( bytecodeMap(i) != 0 ) {
				return bytecodeMap(i)
			}
			i = i - 1;
		}
		0;
	}

    /**
     *
     */
    private def transform(classFile: de.tud.cs.st.bat.ClassFile) {
        val objectType = process_class(classFile.thisClass, Some(classFile.classCategory), true)



        // Note: there is exactly one class (java/lang/Object) that has no superclass
        if( classFile.superClass != null)
        {
            process_extends(objectType, classFile.superClass)
        }
        classFile.interfaces.foreach(i =>
            process_implements(objectType, i)
        )

        classFile.methods.foreach(transform(classFile.thisClass, _))

        classFile.fields.foreach(transform(classFile.thisClass, _))

        classFile.attributes.foreach{
            //case ica : InnerClasses_attribute => transform(classFile.thisClass, ica)
            //case ema : EnclosingMethod_attribute => transform(classFile.thisClass, ema)
            case _ => // do nothing
        }
    }

    /**
     *
     */
    private def transform(declaringClass: de.tud.cs.st.bat.ReferenceType, method_info: Method_Info) {
        process_method(declaringClass, method_info.name, method_info.descriptor.parameterTypes, method_info.descriptor.returnType);

        method_info.attributes.foreach(
        {
            //case code_attribute: Code_attribute => transform(method, code_attribute)
            //case exceptions_attribute: Exceptions_attribute => transform(method, exceptions_attribute)
            case _ => // do nothing for currently unsupported attributes
        })
    }


    /**
     *
     */
    private def transform(declaringClass: de.tud.cs.st.bat.ObjectType, field_info: Field_Info) {
        process_field(declaringClass, field_info.name, field_info.descriptor.fieldType, field_info.isStatic);
    }


    def transform_instruction_default(instr: de.tud.cs.st.bat.Instruction, pc: Int, declaringMethod: Method) {
        // do nothing for process_instruction that we don't want to support yet
    }

/*
    /**
     * The InnerClasses attribute5 is a variable-length attribute in the attributes table
     * of the ClassFile (§4.2) structure. If the constant pool of a class or interface C contains
     * a CONSTANT_Class_info entry which represents a class or interface that is
     * not a member of a package, then C‘s ClassFile structure must have exactly one
     * InnerClasses attribute in its attributes table.
     *
     * If C is a member, the value of the outer_class_info_index item is not zero.
     *
     * If C is anonymous, the value of the inner_name_index item must be zero.
     */
    private def transform(declaringClass: ObjectType, innerClasses: InnerClasses_attribute) {
        innerClasses.classes.foreach( process_inner_class_entry )
    }

    /**
     * The EnclosingMethod attribute is an optional fixed-length attribute in the attributes table of the ClassFile (§4.2) structure.
     * A class must have an EnclosingMethod attribute if and only if it is a local class or an anonymous class.
     * A class may have no more than one EnclosingMethod attribute.
     */
    private def transform(declaringClass: ObjectType, enclosingMethod: EnclosingMethod_attribute) {
        // val source = getMethod(enclosingMethod.clazz, enclosingMethod.name, enclosingMethod.descriptor.parameterTypes, enclosingMethod.descriptor.returnType)
        process_enclosing_method( new unresolved_enclosing_method(
            enclosingMethod.clazz,
            if( enclosingMethod.name eq null ) { None } else { Some(enclosingMethod.name) },
            if( enclosingMethod.descriptor eq null ) { None } else { Some(enclosingMethod.descriptor.parameterTypes) },
            if( enclosingMethod.descriptor eq null ) { None } else { Some(enclosingMethod.descriptor.returnType) },
            declaringClass)
        )
    }

    private def transform(declaringMethod: Method, exceptions_attribute: Exceptions_attribute)
    {
        exceptions_attribute.exceptionTable.foreach( e => process_thrown_exception(new throws(declaringMethod, e)) )
    }

    /**
     * transform the individual bytecode instructions
     */
    private def transform(declaringMethod: Method, code_attribute: Code_attribute)
    {
        var pc = 0

        code_attribute.exceptionTable.foreach( entry => {
                val handler = new ExceptionHandler(
                    declaringMethod,
                    (
                        if( entry.catchType == null)
                            None
                        else
                            Some(entry.catchType)
                    ),
                    entry.startPC,
                    (
                        if( entry.endPC >= code_attribute.bytecodeMap.length )
                            lastInstructionIndex(code_attribute.bytecodeMap)
                        else
                            code_attribute.bytecodeMap(entry.endPC)
                    ),
                    entry.handlerPC
                )
                process_exception_handler(handler)
            }
        )

        code_attribute.code.foreach(instr => {
            transform(instr, pc, code_attribute.bytecodeMap, declaringMethod)
            pc += 1
            }
        )
    }

    def transform_instruction_default(instr: Instruction, pc: Int, declaringMethod: Method) {
        // do nothing for process_instruction that we don't want to support yet
    }

    override def transform_BAT_invokeinterface(instr: BAT_invokeinterface, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val callee = process_method(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = invokeinterface(declaringMethod, pc, callee)
        process_instruction(instruction)
    }

    override def transform_BAT_invokespecial(instr: BAT_invokespecial, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val callee = process_method(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = invokespecial(declaringMethod, pc, callee)
        process_instruction(instruction)
    }

    override def transform_BAT_invokestatic(instr: BAT_invokestatic, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val callee = process_method(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = invokestatic(declaringMethod, pc, callee)
        process_instruction(instruction)
    }

    override def transform_BAT_invokevirtual(instr: BAT_invokevirtual, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = invokevirtual(declaringMethod, pc, callee)
        process_instruction(instruction)
    }

    override def transform_BAT_putstatic(instr: BAT_putstatic, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = process_field(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = putstatic(declaringMethod, pc, field)
        process_instruction(instruction)
    }

    override def transform_BAT_putfield(instr: BAT_putfield, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = process_field(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = putfield(declaringMethod, pc, field)
        process_instruction(instruction)
    }

    override def transform_BAT_getstatic(instr: BAT_getstatic, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = process_field(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = GetStatic(declaringMethod, pc, field)
        process_instruction(instruction)
    }

    override def transform_BAT_getfield(instr: BAT_getfield, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = process_field(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = GetField(declaringMethod, pc, field)
        process_instruction(instruction)
    }

    override def transform_BAT_checkcast(instr: BAT_checkcast, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = CheckCast(declaringMethod, pc, instr.T.asReferenceType)
        process_instruction(instruction)
    }

    override def transform_BAT_newarray(instr: BAT_newarray, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = newarray(declaringMethod, pc, instr.T)
        process_instruction(instruction)
    }

    override def transform_BAT_new(instr: BAT_new, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = `new`(declaringMethod, pc, instr.T.asObjectType)
        process_instruction(instruction)
    }

    override def transform_BAT_instanceof(instr: BAT_instanceof, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = sae.java.model.instructions.InstanceOf(declaringMethod, pc, instr.T.asReferenceType)
        process_instruction(instruction)
    }

    override def transform_BAT_cast(instr: BAT_cast, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = Cast(declaringMethod, pc, instr.S, instr.T)
        process_instruction(instruction)
    }

    override def transform_BAT_push(instr: BAT_push, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        def createPush[T](f: () => T) = new push[T](declaringMethod, pc, f(), instr.T)
        val instruction = createPush(constant_value_function(instr.value))
        process_instruction(instruction)
    }

*/

}