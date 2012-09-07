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
package sae.syntax.sql.compiler

import sae.syntax.sql.ast.predicates.{WhereClauseSequence, Predicate}
import org.junit.Test
import org.junit.Assert._
import sae.syntax.sql.ast.{OrOperator, AndOperator}

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 07.09.12
 * Time: 13:01
 */

class TestNormalizePredicates
{

    private case class TestPredicate(s: String) extends Predicate

    implicit def p(s: String): Predicate = TestPredicate (s)

    @Test
    def testTrivialAndNormalization() {
        val clause = WhereClauseSequence (Seq ("A", AndOperator, "B", AndOperator, "C"))
        val normalized = NormalizePredicates (clause.expressions)
        assertEquals (
            Seq (
                Seq (
                    p ("A"),
                    p ("B"),
                    p ("C")
                )
            ),
            normalized)
    }

    @Test
    def testAndWithSubExpression() {
        val clause = WhereClauseSequence (Seq ("A", AndOperator, WhereClauseSequence (Seq ("B", OrOperator, "C"))))
        val normalized = NormalizePredicates (clause.expressions)
        assertEquals (
            Seq (
                Seq (
                    p ("A"), p ("B")
                ),
                Seq (
                    p ("A"), p ("C")
                )
            ),
            normalized)
    }

    @Test
    def testSubExpressionWithAnd() {
        val clause = WhereClauseSequence (Seq (WhereClauseSequence (Seq ("B", OrOperator, "C")), AndOperator, "A"))
        val normalized = NormalizePredicates (clause.expressions)
        assertEquals (
            Seq (
                Seq (
                    p ("B"), p ("A")
                ),
                Seq (
                    p ("C"), p ("A")
                )
            ),
            normalized)
    }

    @Test
    def testTrivialOrNormalization() {
        val clause = WhereClauseSequence (Seq ("A", OrOperator, "B", OrOperator, "C"))
        val normalized = NormalizePredicates (clause.expressions)
        assertEquals (
            Seq (
                Seq (p ("A")),
                Seq (p ("B")),
                Seq (p ("C"))
            ),
            normalized)
    }

    @Test
    def testNestedOrNormalization() {
        val clause = WhereClauseSequence (Seq ("A", OrOperator, "B", OrOperator, WhereClauseSequence (Seq ("C", OrOperator, "D"))))
        val normalized = NormalizePredicates (clause.expressions)
        assertEquals (
            Seq (
                Seq (p ("A")),
                Seq (p ("B")),
                Seq (p ("C")),
                Seq (p ("D"))
            ),
            normalized)
    }
}
