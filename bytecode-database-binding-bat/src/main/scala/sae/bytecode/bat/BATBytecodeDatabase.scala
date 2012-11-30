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

import java.io.InputStream
import java.util.zip.{ZipEntry, ZipInputStream}
import sae.SetExtent
import de.tud.cs.st.bat.resolved.{ArrayType, ObjectType}
import sae.bytecode.BytecodeDatabase
import sae.syntax.sql._
import sae.bytecode.instructions._
import sae.Relation
import sae.syntax.RelationalAlgebraSyntax
import sae.bytecode.structure.{ClassDeclaration, MethodDeclaration, CodeInfo, FieldDeclaration, InheritanceRelation, CodeAttribute, ExceptionHandlerInfo}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 22.08.12
 * Time: 21:08
 */

class BATBytecodeDatabase
    extends BytecodeDatabase
{


    val reader = new SAEJava6Framework (this)

    val classDeclarations = new SetExtent[ClassDeclaration]

    val methodDeclarations = new SetExtent[MethodDeclaration]

    val fieldDeclarations = new SetExtent[FieldDeclaration]

    val code = new SetExtent[CodeInfo]

    lazy val classDeclarationsMinimal: Relation[sae.bytecode.structure.minimal.ClassDeclaration] =
        compile (SELECT ((c: ClassDeclaration) => sae.bytecode.structure.minimal.ClassDeclaration (c.minorVersion, c.majorVersion, c.accessFlags, c.classType)) FROM classDeclarations).forceToSet

    lazy val methodDeclarationsMinimal: Relation[sae.bytecode.structure.minimal.MethodDeclaration] =
        compile (SELECT ((m: MethodDeclaration) => sae.bytecode.structure.minimal.MethodDeclaration (m.declaringClassType, m.accessFlags, m.name, m.returnType, m.parameterTypes)) FROM methodDeclarations).forceToSet

    lazy val fieldDeclarationsMinimal: Relation[sae.bytecode.structure.minimal.FieldDeclaration] =
        compile (SELECT ((f: FieldDeclaration) => sae.bytecode.structure.minimal.FieldDeclaration (f.declaringClassType, f.accessFlags, f.name, f.fieldType)) FROM fieldDeclarations).forceToSet

    lazy val codeMinimal =
        compile (SELECT ((c: CodeInfo) => sae.bytecode.structure.minimal.CodeInfo (
            sae.bytecode.structure.minimal.MethodDeclaration (c.declaringMethod.declaringClassType, c.declaringMethod.accessFlags, c.declaringMethod.name, c.declaringMethod.returnType, c.declaringMethod.parameterTypes),
            c.code)) FROM code).forceToSet

    lazy val classInheritance: Relation[InheritanceRelation] =
        SELECT ((cd: ClassDeclaration) => InheritanceRelation (cd.classType, cd.superClass.get)) FROM classDeclarations WHERE (_.superClass.isDefined)

    lazy val interfaceInheritance: Relation[InheritanceRelation] =
        SELECT ((cd: ClassDeclaration, i: ObjectType) => InheritanceRelation (cd.classType, i)) FROM (classDeclarations, ((_: ClassDeclaration).interfaces) IN classDeclarations)

    lazy val instructions: Relation[InstructionInfo] = compile (SELECT (*) FROM (identity[List[InstructionInfo]] _ IN instructionInfos)).forceToSet

    private lazy val instructionInfos: Relation[List[InstructionInfo]] = SELECT ((codeInfo: CodeInfo) => {
        var i = 0
        var index = 0
        val length = codeInfo.code.instructions.length
        var result: List[InstructionInfo] = Nil
        while (i < length) {
            val instr = codeInfo.code.instructions (i)
            if (instr != null) {
                result = (InstructionInfo (codeInfo.declaringMethod, instr, i, index)) :: result
                index += 1
            }
            i += 1
        }
        result
    }
    ) FROM code


    lazy val instructionsMinimal: Relation[minimal.InstructionInfo] = compile (SELECT (*) FROM (identity[List[minimal.InstructionInfo]] _ IN instructionInfosMinimal)).forceToSet

    private lazy val instructionInfosMinimal: Relation[List[minimal.InstructionInfo]] = SELECT ((codeInfo: sae.bytecode.structure.minimal.CodeInfo) => {
        var i = 0
        var index = 0
        val length = codeInfo.code.instructions.length
        var result: List[minimal.InstructionInfo] = Nil
        while (i < length) {
            val instr = codeInfo.code.instructions (i)
            if (instr != null) {
                result = (minimal.InstructionInfo (
                    sae.bytecode.structure.minimal.MethodDeclaration (codeInfo.declaringMethod.declaringType, codeInfo.declaringMethod.accessFlags, codeInfo.declaringMethod.name, codeInfo.declaringMethod.returnType, codeInfo.declaringMethod.parameterTypes)
                    , instr, i, index)) :: result
                index += 1
            }
            i += 1
        }
        result
    }
    ) FROM codeMinimal


    lazy val codeAttributes: Relation[CodeAttribute] = SELECT (
        (codeInfo: CodeInfo) => CodeAttribute (
            codeInfo.declaringMethod,
            codeInfo.code.instructions.size,
            codeInfo.code.maxStack,
            codeInfo.code.maxLocals,
            codeInfo.code.exceptionHandlers
        )
    ) FROM code

    val exceptionHandlers = new SetExtent[ExceptionHandlerInfo]

    lazy val inheritance: Relation[InheritanceRelation] = SELECT (*) FROM classInheritance UNION_ALL (SELECT (*) FROM interfaceInheritance)

    lazy val subTypes: Relation[InheritanceRelation] = SELECT ((subType: (ObjectType, ObjectType)) => InheritanceRelation (subType._1, subType._2)) FROM (RelationalAlgebraSyntax.TC[InheritanceRelation, ObjectType](inheritance)(_.subType, _.superType))


    lazy val constructors: Relation[MethodDeclaration] =
        SELECT (*) FROM (methodDeclarations) WHERE (_.name == "<init>")

    lazy val constructorsMinimal =
        compile (SELECT (*) FROM (methodDeclarationsMinimal) WHERE (_.name == "<init>"))


    lazy val invokeStatic: Relation[INVOKESTATIC] =
        SELECT ((_: InstructionInfo).asInstanceOf[INVOKESTATIC]) FROM (instructions) WHERE (_.isInstanceOf[INVOKESTATIC])

    lazy val invokeVirtual: Relation[INVOKEVIRTUAL] =
        SELECT ((_: InstructionInfo).asInstanceOf[INVOKEVIRTUAL]) FROM (instructions) WHERE (_.isInstanceOf[INVOKEVIRTUAL])

    lazy val invokeInterface: Relation[INVOKEINTERFACE] =
        SELECT ((_: InstructionInfo).asInstanceOf[INVOKEINTERFACE]) FROM (instructions) WHERE (_.isInstanceOf[INVOKEINTERFACE])

    lazy val invokeSpecial: Relation[INVOKESPECIAL] =
        SELECT ((_: InstructionInfo).asInstanceOf[INVOKESPECIAL]) FROM (instructions) WHERE (_.isInstanceOf[INVOKESPECIAL])

    lazy val readField: Relation[FieldReadInstruction] =
        SELECT ((_: InstructionInfo).asInstanceOf[FieldReadInstruction]) FROM (instructions) WHERE (_.isInstanceOf[FieldReadInstruction])

    lazy val getStatic: Relation[GETSTATIC] =
        SELECT ((_: FieldReadInstruction).asInstanceOf[GETSTATIC]) FROM (readField) WHERE (_.isInstanceOf[GETSTATIC])

    lazy val getField: Relation[GETFIELD] =
        SELECT ((_: FieldReadInstruction).asInstanceOf[GETFIELD]) FROM (readField) WHERE (_.isInstanceOf[GETFIELD])

    lazy val writeField: Relation[FieldWriteInstruction] =
        SELECT ((_: InstructionInfo).asInstanceOf[FieldWriteInstruction]) FROM (instructions) WHERE (_.isInstanceOf[FieldWriteInstruction])

    lazy val putStatic: Relation[PUTSTATIC] =
        SELECT ((_: FieldWriteInstruction).asInstanceOf[PUTSTATIC]) FROM (writeField) WHERE (_.isInstanceOf[PUTSTATIC])

    lazy val putField: Relation[PUTFIELD] =
        SELECT ((_: FieldWriteInstruction).asInstanceOf[PUTFIELD]) FROM (writeField) WHERE (_.isInstanceOf[PUTFIELD])


    lazy val invokeStaticMinimal: Relation[minimal.INVOKESTATIC] =
        SELECT ((_: minimal.InstructionInfo).asInstanceOf[minimal.INVOKESTATIC]) FROM (instructionsMinimal) WHERE (_.isInstanceOf[minimal.INVOKESTATIC])

    lazy val invokeVirtualMinimal: Relation[minimal.INVOKEVIRTUAL] =
        SELECT ((_: minimal.InstructionInfo).asInstanceOf[minimal.INVOKEVIRTUAL]) FROM (instructionsMinimal) WHERE (_.isInstanceOf[minimal.INVOKEVIRTUAL])

    lazy val invokeInterfaceMinimal: Relation[minimal.INVOKEINTERFACE] =
        SELECT ((_: minimal.InstructionInfo).asInstanceOf[minimal.INVOKEINTERFACE]) FROM (instructionsMinimal) WHERE (_.isInstanceOf[minimal.INVOKEINTERFACE])

    lazy val invokeSpecialMinimal: Relation[minimal.INVOKESPECIAL] =
        SELECT ((_: minimal.InstructionInfo).asInstanceOf[minimal.INVOKESPECIAL]) FROM (instructionsMinimal) WHERE (_.isInstanceOf[minimal.INVOKESPECIAL])

    lazy val readFieldMinimal: Relation[minimal.FieldReadInstruction] =
        SELECT ((_: minimal.InstructionInfo).asInstanceOf[minimal.FieldReadInstruction]) FROM (instructionsMinimal) WHERE (_.isInstanceOf[minimal.FieldReadInstruction])

    lazy val getStaticMinimal: Relation[minimal.GETSTATIC] =
        SELECT ((_: minimal.FieldReadInstruction).asInstanceOf[minimal.GETSTATIC]) FROM (readFieldMinimal) WHERE (_.isInstanceOf[minimal.GETSTATIC])

    lazy val getFieldMinimal: Relation[minimal.GETFIELD] =
        SELECT ((_: minimal.FieldReadInstruction).asInstanceOf[minimal.GETFIELD]) FROM (readFieldMinimal) WHERE (_.isInstanceOf[minimal.GETFIELD])

    lazy val writeFieldMinimal: Relation[minimal.FieldWriteInstruction] =
        SELECT ((_: minimal.InstructionInfo).asInstanceOf[minimal.FieldWriteInstruction]) FROM (instructionsMinimal) WHERE (_.isInstanceOf[minimal.FieldWriteInstruction])

    lazy val putStaticMinimal: Relation[minimal.PUTSTATIC] =
        SELECT ((_: minimal.FieldWriteInstruction).asInstanceOf[minimal.PUTSTATIC]) FROM (writeFieldMinimal) WHERE (_.isInstanceOf[minimal.PUTSTATIC])

    lazy val putFieldMinimal: Relation[minimal.PUTFIELD] =
        SELECT ((_: minimal.FieldWriteInstruction).asInstanceOf[minimal.PUTFIELD]) FROM (writeFieldMinimal) WHERE (_.isInstanceOf[minimal.PUTFIELD])


    def addClassFile(stream: InputStream) {
        reader.ClassFile (() => stream)
    }

    def removeClassFile(stream: InputStream) {

    }

    def updateClassFile (oldStream: InputStream, newStream: InputStream) {

    }


    def addArchive(stream: InputStream) {
        val zipStream: ZipInputStream = new ZipInputStream (stream)
        var zipEntry: ZipEntry = null
        while ((({
            zipEntry = zipStream.getNextEntry
            zipEntry
        })) != null)
        {
            if (!zipEntry.isDirectory && zipEntry.getName.endsWith (".class")) {
                addClassFile (new ZipStreamEntryWrapper (zipStream, zipEntry))
            }
        }
        ObjectType.cache.clear ()
        ArrayType.cache.clear ()
    }

    def removeArchive(stream: InputStream) {

    }


}
