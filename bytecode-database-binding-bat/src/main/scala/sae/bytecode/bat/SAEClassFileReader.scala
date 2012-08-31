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

import de.tud.cs.st.bat.reader.ClassFileReader
import de.tud.cs.st.bat.resolved.reader.{AttributeBinding, ConstantPoolBinding}
import de.tud.cs.st.bat.resolved.ObjectType
import sae.bytecode.structure.{BATDeclaredFieldInfo, BATDeclaredMethodInfo, BATClassDeclaration}


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

    type ClassFile = BATClassDeclaration

    type Class_Info = BATClassDeclaration

    type Method_Info = BATDeclaredMethodInfo
    type Methods <: IndexedSeq[Method_Info]
    val Method_InfoManifest: ClassManifest[Method_Info] = implicitly

    type Field_Info = BATDeclaredFieldInfo
    type Fields <: IndexedSeq[Field_Info]
    val Field_InfoManifest: ClassManifest[Field_Info] = implicitly

    type Interface = ObjectType
    type Interfaces <: IndexedSeq[ObjectType]
    val InterfaceManifest: ClassManifest[Interface] = implicitly

    def Interface(interface_index: Constant_Pool_Index)(implicit cp: Constant_Pool): Interface =
        interface_index.asObjectType

    def Field_Info(access_flags: Int,
                   name_index: Constant_Pool_Index,
                   descriptor_index: Constant_Pool_Index,
                   attributes: Attributes)(
        implicit cp: Constant_Pool): Field_Info =
    {
        val fieldDeclaration = new BATDeclaredFieldInfo (
            access_flags,
            name_index.asString,
            descriptor_index.asFieldType,
            (attributes exists (_ == de.tud.cs.st.bat.resolved.Deprecated)),
            (attributes exists (_ == de.tud.cs.st.bat.resolved.Synthetic))
        )
        database.declared_fields.element_added (fieldDeclaration)
        fieldDeclaration
    }

    def Method_Info(accessFlags: Int,
                    name_index: Int,
                    descriptor_index: Int,
                    attributes: Attributes)(
        implicit cp: Constant_Pool): Method_Info =
    {
        val descriptor = descriptor_index.asMethodDescriptor
        val methodDeclaration = BATDeclaredMethodInfo (
            accessFlags,
            name_index.asString,
            descriptor.returnType,
            descriptor.parameterTypes,
            (attributes exists (_ == de.tud.cs.st.bat.resolved.Deprecated)),
            (attributes exists (_ == de.tud.cs.st.bat.resolved.Synthetic))
        )
        database.declared_methods.element_added (methodDeclaration)
        methodDeclaration
    }

    def ClassFile(minor_version: Int, major_version: Int,
                  access_flags: Int,
                  this_class: Int,
                  super_class: Int,
                  interfaces: Interfaces,
                  fields: Fields,
                  methods: Methods,
                  attributes: Attributes)(
        implicit cp: Constant_Pool): ClassFile =
    {

        val classDeclaration = BATClassDeclaration (this_class.asObjectType,
            access_flags,
            (attributes exists (_ == de.tud.cs.st.bat.resolved.Deprecated)),
            (attributes exists (_ == de.tud.cs.st.bat.resolved.Synthetic))
        )
        database.declared_classes.element_added (classDeclaration)

        classDeclaration
    }

}