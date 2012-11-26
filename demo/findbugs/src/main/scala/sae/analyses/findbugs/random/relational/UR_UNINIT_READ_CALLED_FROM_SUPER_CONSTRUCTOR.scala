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
package sae.analyses.findbugs.random.relational

import sae.Relation
import sae.syntax.sql._
import sae.bytecode.structure.minimal._
import sae.bytecode.BytecodeDatabase
import sae.bytecode.instructions.minimal._
import sae.bytecode.structure.InheritanceRelation

/**
 *
 * @author Ralf Mitschke
 *
 */

object UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR
    extends (BytecodeDatabase => Relation[GETFIELD])
{
    def apply(database: BytecodeDatabase): Relation[GETFIELD] = {
        import database._

        val overrideMethodsWithRelation: Relation[(MethodDeclaration, InheritanceRelation)] =
            SELECT (*) FROM (methodDeclarationsMinimal, classInheritance) WHERE
                (_.name != "<init>") AND
                (!_.isStatic) AND
                (declaringType === subType)



        val overrideMethods: Relation[MethodDeclaration] =
            SELECT ((sm: MethodDeclaration, e: (MethodDeclaration, InheritanceRelation)) => e._1) FROM (methodDeclarationsMinimal, overrideMethodsWithRelation) WHERE
                (((_: MethodDeclaration).declaringType) === ((_: (MethodDeclaration, InheritanceRelation))._2.superType)) AND
                (((_: MethodDeclaration).name) === ((_: (MethodDeclaration, InheritanceRelation))._1.name)) AND
                (((_: MethodDeclaration).returnType) === ((_: (MethodDeclaration, InheritanceRelation))._1.returnType)) AND
                (((_: MethodDeclaration).parameterTypes) === ((_: (MethodDeclaration, InheritanceRelation))._1.parameterTypes))


        val getsFieldInOverRidden: Relation[GETFIELD] =
            SELECT ((f: GETFIELD, m: MethodDeclaration) => f) FROM (getFieldMinimal, overrideMethods) WHERE
                (declaringMethod === (identity[MethodDeclaration] _)) AND
                (targetType === declaringType)

        val calledSuperConstructor: Relation[(INVOKESPECIAL, GETFIELD)] =
            SELECT (*) FROM (invokeSpecialMinimal, getsFieldInOverRidden) WHERE
                (_.declaringMethod.name == "<init>") AND
                (_.name == "<init>") AND
                (declaringClassType === declaringClassType)

        val calls: Relation[InvokeInstruction] =
            SELECT (*) FROM invokeVirtualMinimal.asInstanceOf[Relation[InvokeInstruction]] UNION_ALL (
                SELECT (*) FROM invokeInterfaceMinimal.asInstanceOf[Relation[InvokeInstruction]]
                )

        SELECT ((i: InvokeInstruction, e: (INVOKESPECIAL, GETFIELD)) => e._2) FROM (calls, calledSuperConstructor) WHERE
            (((_: InvokeInstruction).declaringMethod.declaringType) === ((_: (INVOKESPECIAL, GETFIELD))._1.receiverType)) AND
            (((_: InvokeInstruction).declaringMethod.name) === ((_: (INVOKESPECIAL, GETFIELD))._1.name)) AND
            (((_: InvokeInstruction).declaringMethod.parameterTypes) === ((_: (INVOKESPECIAL, GETFIELD))._1.parameterTypes)) AND
            (((_: InvokeInstruction).declaringMethod.returnType) === ((_: (INVOKESPECIAL, GETFIELD))._1.returnType)) AND
            (((_: InvokeInstruction).receiverType) === ((_: (INVOKESPECIAL, GETFIELD))._1.receiverType)) AND
            (((_: InvokeInstruction).name) === ((_: (INVOKESPECIAL, GETFIELD))._2.declaringMethod.name)) AND
            (((_: InvokeInstruction).parameterTypes) === ((_: (INVOKESPECIAL, GETFIELD))._2.declaringMethod.parameterTypes)) AND
            (((_: InvokeInstruction).returnType) === ((_: (INVOKESPECIAL, GETFIELD))._2.declaringMethod.returnType))
    }
}
