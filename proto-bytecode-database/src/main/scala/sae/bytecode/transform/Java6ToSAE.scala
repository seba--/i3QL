package sae
package bytecode
package transform

import sae.Observable
import sae.bytecode.model._
import de.tud.cs.st.bat._
import de.tud.cs.st.bat.instructions._
import scala.collection.immutable.HashSet

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
    classfiles : Observable[ObjectType],
    classfile_methods : Observable[Method],
    classes : Observable[ObjectType],
    methods : Observable[Method],
    instructions : Observable[Instr])
        extends TransformInstruction[Unit, Method] {

    // TODO: ideally we would search for a view on all classes and reuse that here 
    // var internalClasses = new scala.collection.immutable.HashMap[ClassFile, ClassFile]

    var internalMethods = new scala.collection.immutable.HashMap[Method, Method]
/*
    def getClassFile(objectType : ObjectType) : ClassFile = {
        val cf = ClassFile(objectType.packageName, objectType.simpleName)
        val internalized = internalClasses.get(cf)
        if (internalized == None) {
            internalClasses += (cf -> cf)
            cf
        } else {
            internalized.get
        }
    }
*/

    def getMethod(typ : Type, name : String, parameters : Seq[de.tud.cs.st.bat.Type], returnType : de.tud.cs.st.bat.Type) : Method =
        {
    		if(typ.isObjectType)
    		    getMethod(typ.asObjectType, name, parameters, returnType)
    		else
    		    getMethod(typ.asArrayType, name, parameters, returnType)
    		    
        }

    def getMethod(declaringRef : ReferenceType, name : String, parameters : Seq[de.tud.cs.st.bat.Type], returnType : de.tud.cs.st.bat.Type) : Method =
        {
            val m = Method(declaringRef, name, parameters, returnType)
            val internalized = internalMethods.get(m)
            if (internalized == None) {
                internalMethods += (m -> m)
                m
            } else {
                internalized.get
            }
        }

    /**
     *
     */
    def transform(classFile : de.tud.cs.st.bat.ClassFile) : Unit =
        {
            // these classes are always unique, no check is performed
    		/*
            val c = getClassFile(classFile.thisClass)
            classes.element_added(c)
            classfiles.element_added(c)
            classFile.methods.foreach(transform(c, _))
    		*/
            classes.element_added(classFile.thisClass)
            classfiles.element_added(classFile.thisClass)
            classFile.methods.foreach(transform(classFile.thisClass, _))
        }

    /**
     *
     */
    def transform(declaringClass : ObjectType, method_info : Method_Info) : Unit =
        {
            val method = Method(declaringClass, method_info.name, method_info.descriptor.parameterTypes, method_info.descriptor.returnType)
            methods.element_added(method);
            classfile_methods.element_added(method);
            method_info.attributes.foreach(
                {
                    case code_attribute : Code_attribute => transform(method, code_attribute)
                    case _                               => // do nothing for currently unsupported attributes
                })
        }

    def transform(declaringMethod : Method, code_attribute : Code_attribute) : Unit =
        {
            var pc = 0
            code_attribute.code.foreach(instr =>
                {
                    transform(instr, pc, code_attribute.bytecodeMap, declaringMethod)
                    pc += 1
                }
            )
        }

    def transform_instruction_default(instr : Instruction, pc : Int, declaringMethod : Method) : Unit =
        {
            // do nothing for instructions that we don't want to support yet
        }

    override def transform_BAT_invokeinterface(instr : BAT_invokeinterface, pc : Int, bytecodeMap : Array[Int], declaringMethod : Method) : Unit =
        {
            val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
            val instruction = Instr(declaringMethod, pc, "invoke", InvokeParameters("interface", callee))
            instructions.element_added(instruction)
        }

    override def transform_BAT_invokespecial(instr : BAT_invokespecial, pc : Int, bytecodeMap : Array[Int], declaringMethod : Method) : Unit =
        {
            val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
            val instruction = Instr(declaringMethod, pc, "invoke", InvokeParameters("special", callee))
            instructions.element_added(instruction)
        }

    override def transform_BAT_invokestatic(instr : BAT_invokestatic, pc : Int, bytecodeMap : Array[Int], declaringMethod : Method) : Unit =
        {
            val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
            val instruction = Instr(declaringMethod, pc, "invoke", InvokeParameters("static", callee))
            instructions.element_added(instruction)
        }

    override def transform_BAT_invokevirtual(instr : BAT_invokevirtual, pc : Int, bytecodeMap : Array[Int], declaringMethod : Method) : Unit =
        {
            val callee = getMethod(instr.declaring_class_type, instr.method_name.toUTF8, instr.method_parameters.asFieldTypeSeq, instr.method_return_type)
            val instruction = Instr(declaringMethod, pc, "invoke", InvokeParameters("virtual", callee))
            instructions.element_added(instruction)
        }

}