/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.bytecode.bat

import java.io.DataInputStream
import de.tud.cs.st.bat.reader.ClassFileReader
import de.tud.cs.st.bat.resolved.reader.{AttributeBinding, ConstantPoolBinding}
import de.tud.cs.st.bat.resolved.{Instruction, Code, ObjectType}
import sae.bytecode.structure._
import sae.bytecode.instructions.InstructionInfo


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 23.08.12
 * Time: 13:13
 */

trait SAEClassFileReader
    extends ClassFileReader
    with ConstantPoolBinding
    with AttributeBinding
{
    def database: BATBytecodeDatabase

    type ClassFile = ClassDeclaration

    type Class_Info = ClassDeclaration

    type Method_Info = MethodDeclaration
    type Methods <: IndexedSeq[Method_Info]
    val Method_InfoManifest: ClassManifest[Method_Info] = implicitly

    type Field_Info = FieldDeclaration
    type Fields <: IndexedSeq[Field_Info]
    val Field_InfoManifest: ClassManifest[Field_Info] = implicitly

    type Interface = ObjectType
    type Interfaces <: IndexedSeq[ObjectType]
    val InterfaceManifest: ClassManifest[Interface] = implicitly

    protected def Class_Info(minor_version: Int, major_version: Int, in: DataInputStream)(implicit cp: Constant_Pool): Class_Info = {

        val classInfo = ClassDeclaration (minor_version,
            major_version,
            in.readUnsignedShort,
            in.readUnsignedShort.asObjectType)

        val super_type = in.readUnsignedShort
        if (super_type != 0) {
            database.classInheritance.element_added (InheritanceRelation (super_type.asObjectType, classInfo.classType))
        }
        database.classDeclarations.element_added (classInfo)
        classInfo
    }


    def Interface(declaringClass: Class_Info, interface_index: Constant_Pool_Index)(implicit cp: Constant_Pool): Interface = {
        val interface = interface_index.asObjectType
        database.interfaceInheritance.element_added (InheritanceRelation (interface, declaringClass.classType))
        interface
    }


    def Field_Info(declaringClass: Class_Info,
                   access_flags: Int,
                   name_index: Constant_Pool_Index,
                   descriptor_index: Constant_Pool_Index,
                   attributes: Attributes)(
        implicit cp: Constant_Pool): Field_Info =
    {
        val fieldDeclaration = new FieldDeclaration (
            declaringClass,
            access_flags,
            name_index.asString,
            descriptor_index.asFieldType)
        database.fieldDeclarations.element_added (fieldDeclaration)
        fieldDeclaration
    }

    def Method_Info(declaringClass: Class_Info,
                    accessFlags: Int,
                    name_index: Int,
                    descriptor_index: Int,
                    attributes: Attributes)(
        implicit cp: Constant_Pool): Method_Info =
    {
        val descriptor = descriptor_index.asMethodDescriptor
        val methodDeclaration = MethodDeclaration (
            declaringClass,
            accessFlags,
            name_index.asString,
            descriptor.returnType,
            descriptor.parameterTypes
        )
        database.methodDeclarations.element_added (methodDeclaration)
        attributes.foreach (_ match {
            case code: Code => addInstructions (methodDeclaration, code.instructions)
            case _ => /* do nothing*/
        })
        methodDeclaration
    }

    def addInstructions(declaringMethod: Method_Info, instructions: Array[Instruction]) {
        var i = 0
        var index = 0
        while (i < instructions.length)
        {
            val instr = instructions (i)
            i += 1
            if (instr != null) {
                index += 1
                database.instructions.element_added (InstructionInfo (declaringMethod, instr, i, index))
            }

        }
    }

    def ClassFile(classInfo: Class_Info,
                  interfaces: Interfaces,
                  fields: Fields,
                  methods: Methods,
                  attributes: Attributes)(
        implicit cp: Constant_Pool): ClassFile =
    {
        classInfo
    }

}