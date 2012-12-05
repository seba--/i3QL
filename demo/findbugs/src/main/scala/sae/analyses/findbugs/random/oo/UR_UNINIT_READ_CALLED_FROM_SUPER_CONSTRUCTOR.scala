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
package sae.analyses.findbugs.random.oo

import sae.Relation
import sae.syntax.sql._
import sae.bytecode._
import sae.bytecode.instructions._
import structure.FieldDeclaration

/**
 *
 * @author Ralf Mitschke
 *
 */

object UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR
    extends (BytecodeDatabase => Relation[(GETFIELD, InvokeInstruction)])
{
    def apply(database: BytecodeDatabase): Relation[(GETFIELD, InvokeInstruction)] = {
        import database._

        val selfCallsFromConstructor = compile (
            SELECT (*) FROM invokeVirtual.asInstanceOf[Relation[InvokeInstruction]] WHERE
                (i => i.declaringMethod.declaringClassType == i.receiverType) AND
                (_.declaringMethod.name == "<init>")
            /*
            UNION_ALL (
            SELECT (*) FROM invokeInterface.asInstanceOf[Relation[InvokeInstruction]]
            )
            */
        )

        assert (!sae.ENABLE_FORCE_TO_SET || selfCallsFromConstructor.isSet)

        val superCalls = compile (
            SELECT (*) FROM invokeSpecial WHERE
                (_.declaringMethod.name == "<init>") AND
                (_.name == "<init>") AND
                (_.declaringMethod.declaringClass.superClass.isDefined) AND
                (i => i.declaringMethod.declaringClass.superClass.get == i.receiverType)
        )

        assert (!sae.ENABLE_FORCE_TO_SET || superCalls.isSet)

        /*
        val selfCallsFromCalledConstructor = compile (
            SELECT ((selfCall: InvokeInstruction, superCall: INVOKESPECIAL) => selfCall) FROM (selfCallsFromConstructor, superCalls) WHERE
                //SELECT (*) FROM (selfCallsFromConstructor, superCalls) WHERE
                (((_: InvokeInstruction).declaringMethod.declaringClassType) === ((_: INVOKESPECIAL).receiverType)) AND
                (((_: InvokeInstruction).declaringMethod.name) === ((_: INVOKESPECIAL).name)) AND
                (((_: InvokeInstruction).declaringMethod.returnType) === ((_: INVOKESPECIAL).returnType)) AND
                (((_: InvokeInstruction).declaringMethod.parameterTypes) === ((_: INVOKESPECIAL).parameterTypes))

                //(declaringMethod === referencedMethod)
        )
        */

        val selfCallsFromCalledConstructor = compile (
            SELECT (*) FROM (selfCallsFromConstructor) WHERE EXISTS (
                SELECT (*) FROM (superCalls) WHERE
                    (((_: INVOKESPECIAL).receiverType) === ((_: InvokeInstruction).declaringMethod.declaringClassType)) AND
                    (((_: INVOKESPECIAL).name) === ((_: InvokeInstruction).declaringMethod.name)) AND
                    (((_: INVOKESPECIAL).returnType) === ((_: InvokeInstruction).declaringMethod.returnType)) AND
                    (((_: INVOKESPECIAL).parameterTypes) === ((_: InvokeInstruction).declaringMethod.parameterTypes))
            )
            //(declaringMethod === referencedMethod)
        )

        val selfFieldReads = compile (
            SELECT ((get: GETFIELD, f: FieldDeclaration) => get) FROM (getField, fieldDeclarations) WHERE
                (i => i.declaringMethod.declaringClassType == i.receiverType) AND
                (_.declaringMethod.name != "<init>") AND
                //(declaringClassType === declaringType)
                (((_: GETFIELD).receiverType) === ((_: FieldDeclaration).declaringType)) AND
                (((_: GETFIELD).name) === ((_: FieldDeclaration).name)) AND
                (((_: GETFIELD).fieldType) === ((_: FieldDeclaration).fieldType))
        )

        compile (
            //SELECT ((get: GETFIELD, selfCall: InvokeInstruction) => get) FROM (selfFieldReads, selfCallsFromCalledConstructor) WHERE
            SELECT (*) FROM (selfFieldReads, selfCallsFromCalledConstructor) WHERE
                //(declaringMethod === referencedMethod)
                (((_: GETFIELD).declaringMethod.declaringClass.superClass.get) === ((_: InvokeInstruction).receiverType)) AND
                (((_: GETFIELD).declaringMethod.name) === ((_: InvokeInstruction).name)) AND
                (((_: GETFIELD).declaringMethod.returnType) === ((_: InvokeInstruction).returnType)) AND
                (((_: GETFIELD).declaringMethod.parameterTypes) === ((_: InvokeInstruction).parameterTypes))

        )


        /*
        val overrideMethodsWithRelation: Relation[(MethodDeclaration)] =
            SELECT (*) FROM (methodDeclarations) WHERE
                (_.name != "<init>") AND
                (!_.isStatic)



        val overrideMethods: Relation[MethodDeclaration] =
            SELECT ((sm: MethodDeclaration, e: (MethodDeclaration, InheritanceRelation)) => e._1) FROM (methodDeclarations, overrideMethodsWithRelation) WHERE
                (((_: MethodDeclaration).declaringClassType) === ((_: (MethodDeclaration, InheritanceRelation))._2.superType)) AND
                (((_: MethodDeclaration).name) === ((_: (MethodDeclaration, InheritanceRelation))._1.name)) AND
                (((_: MethodDeclaration).returnType) === ((_: (MethodDeclaration, InheritanceRelation))._1.returnType)) AND
                (((_: MethodDeclaration).parameterTypes) === ((_: (MethodDeclaration, InheritanceRelation))._1.parameterTypes))


        val getsFieldInOverRidden: Relation[GETFIELD] =
            SELECT ((f: GETFIELD, m: MethodDeclaration) => f) FROM (getField, overrideMethods) WHERE
                (declaringMethod === (identity[MethodDeclaration] _)) AND
                (targetType === declaringType)


        val getsDeclaredFieldInOverRidden: Relation[GETFIELD] =
            SELECT ((f: GETFIELD, m: FieldDeclaration) => f) FROM (getsFieldInOverRidden, fieldDeclarations) WHERE
                (((_: GETFIELD).receiverType) === ((_: FieldDeclaration).declaringType)) AND
                (((_: GETFIELD).name) === ((_: FieldDeclaration).name)) AND
                (((_: GETFIELD).fieldType) === ((_: FieldDeclaration).fieldType))

        val calledSuperConstructor: Relation[(INVOKESPECIAL, GETFIELD)] =
            SELECT (*) FROM (invokeSpecial, getsDeclaredFieldInOverRidden) WHERE
                (_.declaringMethod.name == "<init>") AND
                (_.name == "<init>") AND
                (i => i.receiverType != i.declaringMethod.declaringClassType) AND
                (declaringClassType === declaringClassType)

        val calls: Relation[InvokeInstruction] =
            SELECT (*) FROM invokeVirtual.asInstanceOf[Relation[InvokeInstruction]] UNION_ALL (
                SELECT (*) FROM invokeInterface.asInstanceOf[Relation[InvokeInstruction]]
                )

        SELECT ((i: InvokeInstruction, e: (INVOKESPECIAL, GETFIELD)) => e._2) FROM (calls, calledSuperConstructor) WHERE
            (((_: InvokeInstruction).declaringMethod.declaringClassType) === ((_: (INVOKESPECIAL, GETFIELD))._1.receiverType)) AND
            (((_: InvokeInstruction).declaringMethod.name) === ((_: (INVOKESPECIAL, GETFIELD))._1.name)) AND
            (((_: InvokeInstruction).declaringMethod.parameterTypes) === ((_: (INVOKESPECIAL, GETFIELD))._1.parameterTypes)) AND
            (((_: InvokeInstruction).declaringMethod.returnType) === ((_: (INVOKESPECIAL, GETFIELD))._1.returnType)) AND
            (((_: InvokeInstruction).receiverType) === ((_: (INVOKESPECIAL, GETFIELD))._1.receiverType)) AND
            (((_: InvokeInstruction).name) === ((_: (INVOKESPECIAL, GETFIELD))._2.declaringMethod.name)) AND
            (((_: InvokeInstruction).parameterTypes) === ((_: (INVOKESPECIAL, GETFIELD))._2.declaringMethod.parameterTypes)) AND
            (((_: InvokeInstruction).returnType) === ((_: (INVOKESPECIAL, GETFIELD))._2.declaringMethod.returnType))
        */
    }
}
