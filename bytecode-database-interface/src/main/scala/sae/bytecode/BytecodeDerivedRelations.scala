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
package sae.bytecode

import instructions._
import sae.Relation
import structure.{InnerClass, MethodDeclaration, CodeAttribute, InheritanceRelation}
import de.tud.cs.st.bat.resolved.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Date: 07.08.12
 * Time: 11:20
 *
 */
trait BytecodeDerivedRelations
{
    def typeDeclarations: Relation[ObjectType]

    def innerClasses: Relation[InnerClass]

    def classInheritance: Relation[InheritanceRelation]

    def interfaceInheritance: Relation[InheritanceRelation]

    def inheritance: Relation[InheritanceRelation]

    def subTypes: Relation[InheritanceRelation]

    def constructors: Relation[MethodDeclaration]

    def instructions: Relation[InstructionInfo]

    def codeAttributes: Relation[CodeAttribute]

    def invokeStatic: Relation[INVOKESTATIC]

    def invokeVirtual: Relation[INVOKEVIRTUAL]

    def invokeInterface: Relation[INVOKEINTERFACE]

    def invokeSpecial: Relation[INVOKESPECIAL]

    def newObject: Relation[NEW]

    def checkCast: Relation[CHECKCAST]

    def readField: Relation[FieldReadInstruction]

    def getStatic: Relation[GETSTATIC]

    def getField: Relation[GETFIELD]

    def writeField: Relation[FieldWriteInstruction]

    def putStatic: Relation[PUTSTATIC]

    def putField: Relation[PUTFIELD]
}