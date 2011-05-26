package sae
package bytecode
package transform

import sae.Observable
import sae.bytecode.model._
import dependencies.{`extends`, parameter, implements}
import sae.bytecode.model.instructions._
import de.tud.cs.st.bat._
import de.tud.cs.st.bat.instructions._
import reflect.AnyValManifest

/**
 * Transform a classfile to the SAE representation
 * This implementation delivers the following guarantees
 * Every class and method is distinct on the object level.
 * Thus no two objects with the same values exist.
 * Types are currently NOT distinct
 *
 * There is a tradeoff between having distinct classes/methods and
 * looking concrete objects up during construction.
 * Each lookup costs construction time < vs. > each object costs memory
 * Furthermore subsequent queries could make object referential equality checks for better performance.
 */
class Java6ToSAE(
                        classfiles: Observable[ObjectType],
                        classfile_methods: Observable[Method],
                        classfile_fields: Observable[Field],
                        classes: Observable[ObjectType],
                        methods: Observable[Method],
                        fields: Observable[Field],
                        instructions: Observable[Instr[_]],
                        `extends` : Observable[`extends`],
                        implements: Observable[implements],
                        parameter: Observable[parameter])
        extends TransformInstruction[Unit, Method] {

    // TODO: ideally we would search for a view on all classes and reuse that here

    var internalMethods = new scala.collection.immutable.HashMap[Method, Method]

    var internalFields = new scala.collection.immutable.HashMap[Field, Field]


    def getMethod(typ: Type, name: String, parameters: Seq[de.tud.cs.st.bat.Type], returnType: de.tud.cs.st.bat.Type): Method = {
        if (typ.isObjectType)
            getMethod(typ.asObjectType, name, parameters, returnType)
        else
            getMethod(typ.asArrayType, name, parameters, returnType)

    }

    def getMethod(declaringRef: ReferenceType, name: String, parameters: Seq[de.tud.cs.st.bat.Type], returnType: de.tud.cs.st.bat.Type): Method = {
        val m = Method(declaringRef, name, parameters, returnType)
        val internalized = internalMethods.get(m)
        if (internalized == None) {
            internalMethods += (m -> m)
            methods.element_added(m)
            m
        } else {
            internalized.get
        }
    }


    def getField(declaringClass: ObjectType, name: String, fieldType: FieldType): Field = {
        val f = Field(declaringClass, name, fieldType)
        val internalized = internalFields.get(f)
        if (internalized == None) {
            internalFields += (f -> f)
            fields.element_added(f)
            f
        } else {
            internalized.get
        }
    }

    /**
     * Conversions from constant value to a type
     */
    def constant_value_function(constantValue : ConstantValue[_]) : () => _ =
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

    /**
     *
     */
    def transform(classFile: de.tud.cs.st.bat.ClassFile) {
        // these classes are always unique, no check is performed
        classes.element_added(classFile.thisClass)

        classfiles.element_added(classFile.thisClass)

        `extends`.element_added(new `extends`(classFile.thisClass, classFile.superClass))

        classFile.interfaces.foreach(i =>
            implements.element_added(new implements(classFile.thisClass, i))
        )

        classFile.methods.foreach(transform(classFile.thisClass, _))

        classFile.fields.foreach(transform(classFile.thisClass, _))
    }

    /**
     *
     */
    def transform(declaringClass: ObjectType, method_info: Method_Info) {
        /*
        val method = Method(declaringClass, method_info.name, method_info.descriptor.parameterTypes, method_info.descriptor.returnType)
        methods.element_added(method);
        */
        val method = getMethod(declaringClass, method_info.name, method_info.descriptor.parameterTypes, method_info.descriptor.returnType)

        classfile_methods.element_added(method);

        method_info.descriptor.parameterTypes.foreach(p => (
                parameter.element_added(new parameter(method, p)) )
        )

        method_info.attributes.foreach(
        {
            case code_attribute: Code_attribute => transform(method, code_attribute)
            case _ => // do nothing for currently unsupported attributes
        })
    }


    /**
     *
     */
    def transform(declaringClass: ObjectType, field_info: Field_Info) {
        val field = getField(declaringClass, field_info.name, field_info.descriptor.fieldType)
        classfile_fields.element_added(field);
    }

    def transform(declaringMethod: Method, code_attribute: Code_attribute) {
        var pc = 0
        code_attribute.code.foreach(instr => {
            transform(instr, pc, code_attribute.bytecodeMap, declaringMethod)
            pc += 1
        }
        )
    }



    def transform_instruction_default(instr: Instruction, pc: Int, declaringMethod: Method) {
        // do nothing for instructions that we don't want to support yet
    }

    override def transform_BAT_invokeinterface(instr: BAT_invokeinterface, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = invokeinterface(declaringMethod, pc, callee)
        instructions.element_added(instruction)
    }

    override def transform_BAT_invokespecial(instr: BAT_invokespecial, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = invokespecial(declaringMethod, pc, callee)
        instructions.element_added(instruction)
    }

    override def transform_BAT_invokestatic(instr: BAT_invokestatic, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = invokestatic(declaringMethod, pc, callee)
        instructions.element_added(instruction)
    }

    override def transform_BAT_invokevirtual(instr: BAT_invokevirtual, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = invokevirtual(declaringMethod, pc, callee)
        instructions.element_added(instruction)
    }

    override def transform_BAT_putstatic(instr: BAT_putstatic, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = getField(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = putstatic(declaringMethod, pc, field)
        instructions.element_added(instruction)
    }

    override def transform_BAT_putfield(instr: BAT_putfield, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = getField(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = putfield(declaringMethod, pc, field)
        instructions.element_added(instruction)
    }

    override def transform_BAT_getstatic(instr: BAT_getstatic, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = getField(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = getstatic(declaringMethod, pc, field)
        instructions.element_added(instruction)
    }

    override def transform_BAT_getfield(instr: BAT_getfield, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = getField(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = getfield(declaringMethod, pc, field)
        instructions.element_added(instruction)
    }

    override def transform_BAT_checkcast(instr: BAT_checkcast, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = checkcast(declaringMethod, pc, instr.T.asReferenceType)
        instructions.element_added(instruction)
    }

    override def transform_BAT_newarray(instr: BAT_newarray, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = newarray(declaringMethod, pc, instr.T)
        instructions.element_added(instruction)
    }

    override def transform_BAT_new(instr: BAT_new, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = `new`(declaringMethod, pc, instr.T.asObjectType)
        instructions.element_added(instruction)
    }

    override def transform_BAT_instanceof(instr: BAT_instanceof, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = instanceof(declaringMethod, pc, instr.T.asReferenceType)
        instructions.element_added(instruction)
    }

    override def transform_BAT_cast(instr: BAT_cast, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = cast(declaringMethod, pc, instr.S, instr.T)
        instructions.element_added(instruction)
    }

    override def transform_BAT_push(instr: BAT_push, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        def createPush[T](f: () => T) = new push[T](declaringMethod, pc, f(), instr.T)
        val instruction = createPush(constant_value_function(instr.value))
        instructions.element_added(instruction)
    }



}