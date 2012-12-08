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
package sae.analyses.findbugs.random.oo.optimized

import sae.Relation
import sae.syntax.sql._
import sae.bytecode.structure._
import sae.bytecode._
import de.tud.cs.st.bat.resolved.VoidType
import sae.analyses.findbugs.AnalysesOO
import sae.operators.impl.{EquiJoinView, TransactionalEquiJoinView}

/**
 *
 * @author Ralf Mitschke
 *
 */

object UG_SYNC_SET_UNSYNC_GET
    extends (BytecodeDatabase => Relation[(MethodDeclaration, MethodDeclaration)])
{

    def setterName: MethodDeclaration => String = _.name.substring (3)

    def getterName: MethodDeclaration => String = _.name.substring (3)

    def apply(database: BytecodeDatabase): Relation[(MethodDeclaration, MethodDeclaration)] = {
        import database._

        val syncedSetters: Relation[MethodDeclaration] =
            SELECT (*) FROM (methodDeclarations) WHERE
                (!_.isAbstract) AND
                (!_.isStatic) AND
                (!_.isNative) AND
                (!_.isPrivate) AND
                (_.name.startsWith ("set")) AND
                (_.isSynchronized) AND
                (_.parameterTypes.length == 1) AND
                (_.returnType == VoidType) AND
                (!_.declaringClass.isInterface)

        val unsyncedGetters: Relation[MethodDeclaration] =
            SELECT (*) FROM (methodDeclarations) WHERE
                (!_.isAbstract) AND
                (!_.isStatic) AND
                (!_.isNative) AND
                (!_.isPrivate) AND
                (_.name.startsWith ("get")) AND
                (!_.isSynchronized) AND
                (_.parameterTypes == Nil) AND
                (_.returnType != VoidType) AND
                (!_.declaringClass.isInterface)


        if (AnalysesOO.transactional)
            new TransactionalEquiJoinView (
                syncedSetters,
                unsyncedGetters,
                (m: MethodDeclaration) => (m.declaringClassType, setterName (m)),
                (m: MethodDeclaration) => (m.declaringClassType, getterName (m)),
                (m1: MethodDeclaration, m2: MethodDeclaration) => (m1, m2)
            )
        else
            new EquiJoinView (
                syncedSetters,
                unsyncedGetters,
                (m: MethodDeclaration) => (m.declaringClassType, setterName (m)),
                (m: MethodDeclaration) => (m.declaringClassType, getterName (m)),
                (m1: MethodDeclaration, m2: MethodDeclaration) => (m1, m2)
            )
    }

}
