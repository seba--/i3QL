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
package sae.analyses.metrics.base

import sae.bytecode.structure._
import sae.bytecode.instructions._
import de.tud.cs.st.bat.resolved.{ObjectType, FieldType}
import sae.bytecode.structure.InheritanceRelation
import sae.bytecode.instructions.NEW
import sae.bytecode.instructions.CHECKCAST
import sae.bytecode.structure.ExceptionDeclaration

/**
 *
 * @author Ralf Mitschke
 *
 */

object DependencyFactory
{


    def extendsDependency: InheritanceRelation => Dependency = {
        rel =>
            Dependency (rel.subType, rel.superType)
    }

    def implementsDependency: InheritanceRelation => Dependency = {
        rel =>
            Dependency (rel.subType, rel.superType)
    }

    def invokeInterfaceDependency: InvokeInstruction => Dependency = {
        rel =>
            Dependency (rel.declaringMethod.declaringClassType, rel.receiverType.asInstanceOf[ObjectType])
    }


    def invokeSpecialDependency: InvokeInstruction => Dependency = {
        rel =>
            Dependency (rel.declaringMethod.declaringClassType, rel.receiverType.asInstanceOf[ObjectType])
    }


    def invokeVirtualDependency: InvokeInstruction => Dependency = {
        rel =>
            Dependency (rel.declaringMethod.declaringClassType, rel.receiverType.asInstanceOf[ObjectType])
    }


    def invokeStaticDependency: InvokeInstruction => Dependency = {
        rel =>
            Dependency (rel.declaringMethod.declaringClassType, rel.receiverType.asInstanceOf[ObjectType])
    }


    def readFieldDependency: FieldReadInstruction => Dependency = {
        rel =>
            Dependency (rel.declaringMethod.declaringClassType, rel.receiverType)
    }


    def writeFieldDependency: FieldWriteInstruction => Dependency = {
        rel =>
            Dependency (rel.declaringMethod.declaringClassType, rel.receiverType)
    }


    def newObjectDependency: NEW => Dependency = {
        rel =>
            Dependency (rel.declaringMethod.declaringClassType, rel.instruction.objectType)
    }


    def checkCastDependency: CHECKCAST => Dependency = {
        rel =>
            Dependency (rel.declaringMethod.declaringClassType, rel.instruction.referenceType.asInstanceOf[ObjectType])
    }


    def exceptionDeclarationDependency: ExceptionDeclaration => Dependency = {
        rel =>
            Dependency (rel.declaringMethod.declaringClassType, rel.exception)
    }

    def fieldDeclarationDependency: FieldDeclaration => Dependency = {
        rel =>
            Dependency (rel.declaringClassType, rel.fieldType.asInstanceOf[ObjectType])
    }

    def parameterTypeDependency: (MethodDeclaration, FieldType) => Dependency = {
        (m, t) =>
            Dependency (m.declaringClassType, t.asInstanceOf[ObjectType])
    }

    def returnTypeDependency: MethodDeclaration => Dependency = {
        rel =>
            Dependency (rel.declaringClassType, rel.returnType.asInstanceOf[ObjectType])
    }
}
