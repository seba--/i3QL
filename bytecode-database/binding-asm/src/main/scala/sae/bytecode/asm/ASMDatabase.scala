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
package sae.bytecode.asm

import sae.bytecode.BytecodeDatabase
import sae.bytecode.asm.reader.ASMProcessor

/**
 *
 * @author Ralf Mitschke
 */
class ASMDatabase
    extends BytecodeDatabase
    with ASMTypes
    with ASMTypeConstructors
    with ASMStructure
    with ASMStructureRelations
    with ASMStructureDerived
    with ASMStructureDerivedConstructors
    with ASMInstructions
    with ASMInstructionsRelations
    with ASMDatabaseManipulation
{

    def additionProcessor = AdditionProcessor

    def removalProcessor = RemovalProcessor

    object AdditionProcessor extends ASMProcessor
    {
        val database: ASMDatabase.this.type = ASMDatabase.this

        def processClassDeclaration (classDeclaration: ClassDeclaration) =
            database.classDeclarations += classDeclaration

        def processMethodDeclaration (methodDeclaration: MethodDeclaration) =
            database.methodDeclarations += methodDeclaration

        def processFieldDeclaration (fieldDeclaration: FieldDeclaration) =
            database.fieldDeclarations += fieldDeclaration

        /*
        def processCodeAttribute (codeAttribute: CodeAttribute) =
            database.codeAttributes += codeAttribute

        def processInstruction (instruction: Instruction) =
            database.instructions += instruction

        def processInnerClassAttribute (innerClassAttribute: InnerClassAttribute) =
            database.innerClassAttributes += innerClassAttribute

        def processEnclosingMethodAttribute (enclosingMethodAttribute: EnclosingMethodAttribute) =
            database.enclosingMethodAttributes += enclosingMethodAttribute
          */
    }


    object RemovalProcessor extends ASMProcessor
    {
        val database: ASMDatabase.this.type = ASMDatabase.this

        def processClassDeclaration (classDeclaration: ClassDeclaration) =
            database.classDeclarations -= classDeclaration

        def processMethodDeclaration (methodDeclaration: MethodDeclaration) =
            database.methodDeclarations -= methodDeclaration

        def processFieldDeclaration (fieldDeclaration: FieldDeclaration) =
            database.fieldDeclarations -= fieldDeclaration

        /*
        def processCodeAttribute (codeAttribute: CodeAttribute) =
            database.codeAttributes -= codeAttribute

        def processInstruction (instruction: Instruction) =
            database.instructions -= instruction

        def processInnerClassAttribute (innerClassAttribute: InnerClassAttribute) =
            database.innerClassAttributes -= innerClassAttribute

        def processEnclosingMethodAttribute (enclosingMethodAttribute: EnclosingMethodAttribute) =
            database.enclosingMethodAttributes -= enclosingMethodAttribute
          */
    }

}
