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
import de.tud.cs.st.bat.resolved._
import sae.bytecode.structure._
import internal.UnresolvedEnclosingMethod
import internal.UnresolvedInnerClassEntry
import internal.{UnresolvedEnclosingMethod, UnresolvedInnerClassEntry}
import sae.bytecode.structure.CodeInfo
import scala.Some
import de.tud.cs.st.bat.resolved.Code
import de.tud.cs.st.bat.resolved.InnerClassTable
import de.tud.cs.st.bat.resolved.EnclosingMethod


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 23.08.12
 * Time: 13:13
 */

trait SAEClassFileEventRemover
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

    protected def Class_Info(minor_version: Int, major_version: Int, in: DataInputStream)
                            (implicit cp: Constant_Pool): Class_Info =
    {
        val accessFlags = in.readUnsignedShort
        val thisClass = in.readUnsignedShort.asObjectType
        val super_class = in.readUnsignedShort
        val classInfo = ClassDeclaration (
            minor_version, major_version,
            accessFlags,
            thisClass,
            // to handle the special case that this class file represents java.lang.Object
            if (super_class == 0) None else Some (super_class.asObjectType),
            Interfaces (thisClass, in, cp)
        )
        database.classDeclarations.element_removed (classInfo)
        classInfo
    }


    def Interface(declaringClass: ObjectType, interface_index: Constant_Pool_Index)
                 (implicit cp: Constant_Pool): Interface =
    {
        interface_index.asObjectType
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
        database.fieldDeclarations.element_removed (fieldDeclaration)
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
        val methodDeclaration = new MethodDeclaration (
            declaringClass,
            accessFlags,
            name_index.asString,
            descriptor.returnType,
            descriptor.parameterTypes
        )
        database.methodDeclarations.element_removed (methodDeclaration)

        database.code.element_removed (
            CodeInfo (methodDeclaration,
                attributes.collectFirst {
                    case code: Code => code
                }.getOrElse(return methodDeclaration),
                attributes.collectFirst {
                    case ex: ExceptionTable => ex
                }.getOrElse(ExceptionTable(Nil)).exceptions
            )
        )
        methodDeclaration
    }

    def ClassFile(classInfo: Class_Info,
                  fields: Fields,
                  methods: Methods,
                  attributes: Attributes)(
        implicit cp: Constant_Pool): ClassFile =
    {
        attributes.foreach {
            case ica: InnerClassTable => ica.innerClasses.foreach (
                ic => database.unresolvedInnerClasses.element_removed (
                    new UnresolvedInnerClassEntry (
                        classInfo.classType,
                        ic.innerClassType,
                        ic.outerClassType,
                        ic.innerName,
                        ic.innerClassAccessFlags
                    )
                )
            )
            case ema: EnclosingMethod => database.unresolvedEnclosingMethods.element_removed (
                new UnresolvedEnclosingMethod (
                    classInfo.classType,
                    if (ema.name != null)
                        Some (ema.name)
                    else
                        None
                    ,
                    if (ema.descriptor != null)
                        Some (ema.descriptor.parameterTypes)
                    else
                        None
                    ,
                    if (ema.descriptor != null)
                        Some (ema.descriptor.returnType)
                    else
                        None
                    ,
                    ema.clazz
                )
            )
            case _ => // do nothing
        }

        classInfo
    }


}