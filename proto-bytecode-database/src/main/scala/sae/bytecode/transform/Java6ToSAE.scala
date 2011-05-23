package sae
package bytecode
package transform

import sae.Observable
import sae.bytecode.model._
import de.tud.cs.st.bat._
import de.tud.cs.st.bat.instructions._
import dependencies._

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
                        classes: Observable[ObjectType],
                        methods: Observable[Method],
                        fields: Observable[Field],
                        instructions: Observable[Instr],
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
            f
        } else {
            internalized.get
        }
    }

    /**
     *
     */
    def transform(classFile: de.tud.cs.st.bat.ClassFile): Unit = {
        // these classes are always unique, no check is performed
        classes.element_added(classFile.thisClass)

        classfiles.element_added(classFile.thisClass)

        classFile.methods.foreach(transform(classFile.thisClass, _))

        `extends`.element_added(new `extends`(classFile.thisClass, classFile.superClass))

        classFile.interfaces.foreach(i =>
            implements.element_added(new implements(classFile.thisClass, i))
        )
    }

    /**
     *
     */
    def transform(declaringClass: ObjectType, method_info: Method_Info): Unit = {
        val method = Method(declaringClass, method_info.name, method_info.descriptor.parameterTypes, method_info.descriptor.returnType)

        methods.element_added(method);

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
    def transform(declaringClass: ObjectType, field_info: Field_Info): Unit = {
        val field = getField(declaringClass, field_info.name, field_info.descriptor.fieldType)
        fields.element_added(field);
    }

    def transform(declaringMethod: Method, code_attribute: Code_attribute): Unit = {
        var pc = 0
        code_attribute.code.foreach(instr => {
            transform(instr, pc, code_attribute.bytecodeMap, declaringMethod)
            pc += 1
        }
        )
    }

    def transform_instruction_default(instr: Instruction, pc: Int, declaringMethod: Method): Unit = {
        // do nothing for instructions that we don't want to support yet
    }

    override def transform_BAT_invokeinterface(instr: BAT_invokeinterface, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method): Unit = {
        val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = Instr(declaringMethod, pc, "invoke", InvokeParameters("interface", callee))
        instructions.element_added(instruction)
    }

    override def transform_BAT_invokespecial(instr: BAT_invokespecial, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method): Unit = {
        val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = Instr(declaringMethod, pc, "invoke", InvokeParameters("special", callee))
        instructions.element_added(instruction)
    }

    override def transform_BAT_invokestatic(instr: BAT_invokestatic, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method): Unit = {
        val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = Instr(declaringMethod, pc, "invoke", InvokeParameters("static", callee))
        instructions.element_added(instruction)
    }

    override def transform_BAT_invokevirtual(instr: BAT_invokevirtual, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method): Unit = {
        val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
        val instruction = Instr(declaringMethod, pc, "invoke", InvokeParameters("virtual", callee))
        instructions.element_added(instruction)
    }

    override def transform_BAT_putstatic(instr: BAT_putstatic, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = getField(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = Instr(declaringMethod, pc, "putstatic_field", field)
        instructions.element_added(instruction)
    }

    override def transform_BAT_putfield(instr: BAT_putfield, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = getField(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = Instr(declaringMethod, pc, "putfield", field)
        instructions.element_added(instruction)
    }

    override def transform_BAT_getstatic(instr: BAT_getstatic, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = getField(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = Instr(declaringMethod, pc, "getstatic_field", field)
        instructions.element_added(instruction)
    }

    override def transform_BAT_getfield(instr: BAT_getfield, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val field = getField(instr.declaringClass.asObjectType, instr.fieldName.toUTF8, instr.fieldType.asFieldType)
        val instruction = Instr(declaringMethod, pc, "getfield", field)
        instructions.element_added(instruction)
    }

    override def transform_BAT_checkcast(instr: BAT_checkcast, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = Instr(declaringMethod, pc, "checkcast", instr.T)
    }

    override def transform_BAT_newarray(instr: BAT_newarray, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = Instr(declaringMethod, pc, "new_array", instr.T)
    }

    override def transform_BAT_new(instr: BAT_new, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = Instr(declaringMethod, pc, "new", instr.T)
    }

    override def transform_BAT_instanceof(instr: BAT_instanceof, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = Instr(declaringMethod, pc, "instanceof", instr.T)
    }

    override def transform_BAT_cast(instr: BAT_cast, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {
        val instruction = Instr(declaringMethod, pc, "cast", TypeCast(instr.S, instr.T))
    }

    override def transform_BAT_push(instr: BAT_push, pc: Int, bytecodeMap: Array[Int], declaringMethod: Method) {

    }
}