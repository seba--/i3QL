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
package sae.bytecode.asm.reader

import org.objectweb.asm._
import sae.bytecode.BytecodeDatabase
import sae.bytecode.asm.ASMDatabase
/*import sae.bytecode.asm.structure.MethodDeclaration
import sae.bytecode.asm.structure.EnclosingMethodAttribute
import sae.bytecode.asm.structure.ClassDeclaration
import sae.bytecode.asm.structure.InnerClassAttribute
import sae.bytecode.asm.structure.FieldDeclaration  */

/**
 *
 * @author Ralf Mitschke
 */
trait ASMClassProcessor
{
    val database: ASMDatabase

    import database._

    def processClassDeclaration (classDeclaration: ClassDeclaration)

    def processMethodDeclaration (methodDeclaration: MethodDeclaration)

    def processFieldDeclaration (fieldDeclaration: FieldDeclaration)

    def processInnerClassAttribute (innerClassAttribute: InnerClassAttribute)

    def processEnclosingMethodAttribute (enclosingMethodAttribute: EnclosingMethodAttribute)

    def classVisitor: ClassVisitor =
        new ASMClassVisitor

    def methodVisitor (methodDeclaration: MethodDeclaration): MethodVisitor

    def fieldVisitor (fieldDeclaration: FieldDeclaration): FieldVisitor

    class ASMClassVisitor
        extends ClassVisitor (Opcodes.ASM4)
    {

        var classDeclaration: ClassDeclaration = null

        override def visit (
            version: Int,
            access: Int,
            name: String,
            signature: String,
            superName: String,
            interfaces: Array[String]
        ) {
            val classType = Type.getObjectType (name)
            val superType = if (superName == null) None else Some (Type.getObjectType (superName))
            val interfaceTypes = interfaces.map (Type.getObjectType)

            classDeclaration = sae.bytecode.asm.structure.ClassDeclaration (
                version,
                access,
                classType,
                superType,
                interfaceTypes
            )

            processClassDeclaration (classDeclaration)
        }

        override def visitMethod (
            access: Int,
            name: String,
            desc: String,
            signature: String,
            exceptions: Array[String]
        ): MethodVisitor = {
            val parameterTypes = Type.getArgumentTypes (desc)
            val returnType = Type.getReturnType (desc)

            val methodDeclaration = sae.bytecode.asm.structure.MethodDeclaration (
                classDeclaration,
                access,
                name,
                returnType,
                parameterTypes
            )

            processMethodDeclaration (methodDeclaration)

            methodVisitor (methodDeclaration)
        }

        override def visitField (access: Int,
            name: String,
            desc: String,
            signature: String,
            value: Any
        ): FieldVisitor = {
            val fieldType = Type.getType (desc)

            val fieldDeclaration = sae.bytecode.asm.structure.FieldDeclaration (
                classDeclaration,
                access,
                name,
                fieldType,
                if (value == null) None else Some (value),
                if (value == null) None else Some (Type.getType(value.getClass)) // TODO correct for primitives
            )

            processFieldDeclaration (fieldDeclaration)

            fieldVisitor (fieldDeclaration)
        }

        override def visitInnerClass (name: String, outerName: String, innerNameInternal: String, access: Int) {
            val innerClassType = Type.getObjectType (name)
            val outerClassType = if (outerName == null) None else Some (Type.getObjectType (outerName))
            val innerName = if (innerNameInternal == null) None else Some (innerNameInternal)
            val innerClassAttribute =
				sae.bytecode.asm.structure.InnerClassAttribute (classDeclaration, innerClassType, outerClassType, innerName, access)
            processInnerClassAttribute (innerClassAttribute)
        }

        override def visitOuterClass (owner: String, nameInternal: String, desc: String) {
            val outerClassType = Type.getObjectType (owner)
            val name = if (nameInternal == null) None else Some (nameInternal)
            val parameterTypes: Option[Seq[Type]] = if (desc == null) None else Some (Type.getArgumentTypes (desc))
            val returnType = if (desc == null) None else Some (Type.getReturnType (desc))
            val enclosingMethodAttribute =
				sae.bytecode.asm.structure.EnclosingMethodAttribute (classDeclaration, outerClassType, name, parameterTypes, returnType)
            processEnclosingMethodAttribute (enclosingMethodAttribute)
        }

    }

}
