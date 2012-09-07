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
package sae.syntax.sql

import ast.WhereClauseExpression

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:46
 *
 * To avoid ambiguities when applying implicit conversions subexpressions have an explicit start that is not
 * accepted as parameter. Thus anonymous functions will always be treated as applied to the actual where clause (or the subexpresion they are part of).
 * In other words, there are no subexpressions without an AND or OR.
 * Also a consequence multiple parenthesis over a single expression will always just apply the inner expression.
 */
trait WHERE_CLAUSE_EXPRESSION_2[DomainA <: AnyRef, DomainB <: AnyRef]
    extends WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]
{

    def AND(predicateA: DomainA => Boolean): WHERE_CLAUSE_EXPRESSION_2[DomainA, DomainB]

    def OR(predicateA: DomainA => Boolean): WHERE_CLAUSE_EXPRESSION_2[DomainA, DomainB]

    def AND[RangeA, RangeB](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_EXPRESSION_2[DomainA, DomainB]

    def OR[RangeA, RangeB](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]): WHERE_CLAUSE_EXPRESSION_2[DomainA, DomainB]

    def AND(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]): WHERE_CLAUSE_EXPRESSION_2[DomainA, DomainB]

    def OR(subExpression: WHERE_CLAUSE_FINAL_SUB_EXPRESSION_2[DomainA, DomainB]): WHERE_CLAUSE_EXPRESSION_2[DomainA, DomainB]

}
