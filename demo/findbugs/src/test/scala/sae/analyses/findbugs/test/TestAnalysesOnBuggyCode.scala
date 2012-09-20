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
package sae.analyses.findbugs.test

import sae.bytecode.bat.BATDatabaseFactory
import sae.analyses.findbugs._
import sae.collections.QueryResult
import sae.bytecode.instructions.{InstructionInfo, INVOKESPECIAL, INVOKEVIRTUAL}
import org.junit.Test
import sae.bytecode.structure.{ClassDeclaration, FieldDeclaration}
import sae.Relation
import sae.syntax.sql.SELECT
import de.tud.cs.st.bat.resolved.FieldType
import sae.bytecode._
import instructions.INVOKESPECIAL
import instructions.INVOKEVIRTUAL
import structure.FieldDeclaration

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 09.09.12
 * Time: 11:19
 */

class TestAnalysesOnBuggyCode
{

    def getStream = this.getClass.getClassLoader.getResourceAsStream ("buggy.jar")

    @Test
    def test_BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION() {
        import sae.collections.Conversions._
        val database = BATDatabaseFactory.create ()
        val analysis: QueryResult[INVOKEVIRTUAL] = BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION (database)

        import sae.syntax.sql._
        val invokeSpecial: QueryResult[INVOKESPECIAL] = sae.collections.Conversions.lazyViewToResult(SELECT ((_: InstructionInfo).asInstanceOf[INVOKESPECIAL]) FROM (database.instructions) WHERE (_.isInstanceOf[INVOKESPECIAL]))
        val invokeVirtual: QueryResult[INVOKEVIRTUAL] = sae.collections.Conversions.lazyViewToResult(SELECT ((_: InstructionInfo).asInstanceOf[INVOKEVIRTUAL]) FROM (database.instructions) WHERE (_.isInstanceOf[INVOKEVIRTUAL]))

        val firstParamType: INVOKESPECIAL => FieldType = _.parameterTypes (0)

        val result: QueryResult[(INVOKESPECIAL,INVOKEVIRTUAL)] = sae.collections.Conversions.lazyViewToResult(
            SELECT (*) FROM
            (invokeSpecial, invokeVirtual) WHERE
            (declaringMethod === declaringMethod) AND
            (receiverType === receiverType) AND
            (sequenceIndex === ((second: INVOKEVIRTUAL) => second.sequenceIndex - 1))  AND
            NOT (firstParamType === returnType)                 AND

            (_.declaringMethod.declaringClass.majorVersion >= 49) AND
            (_.receiverType.isObjectType)  AND
            (_.receiverType.asInstanceOf[ClassType].className.startsWith ("java/lang")) AND
            ((_: INVOKEVIRTUAL).parameterTypes == Nil)  AND
            (_.name.endsWith ("Value"))

        )
        database.addArchive (getStream)

        println (result.size)
        result.foreach (println)

        println (analysis.size)
        analysis.foreach (println)
    }

    @Test
    def test_CI_CONFUSED_INHERITANCE() {
        import sae.collections.Conversions._
        val database = BATDatabaseFactory.create ()
        val analysis: QueryResult[FieldDeclaration] = CI_CONFUSED_INHERITANCE (database)

        database.addArchive (getStream)
        println (analysis.size)
        analysis.foreach (println)
    }

    @Test
    def test_FI_PUBLIC_SHOULD_BE_PROTECTED() {

        import sae.collections.Conversions._
        val database = BATDatabaseFactory.create ()
        val analysis: QueryResult[ClassDeclaration] = FI_PUBLIC_SHOULD_BE_PROTECTED (database)

        database.addArchive (getStream)
        println (analysis.size)
        analysis.foreach (println)
    }
}
