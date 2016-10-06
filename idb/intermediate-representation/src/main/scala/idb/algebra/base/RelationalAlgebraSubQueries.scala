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
package idb.algebra.base

import idb.query.QueryEnvironment

import scala.language.implicitConversions
import scala.language.higherKinds

/**
 *
 * @author Ralf Mitschke
 *
 */

trait RelationalAlgebraSubQueries
    extends RelationalAlgebraBasicOperators
    with RelationalAlgebraSetTheoryOperators
{

    /**
     * Represents a sub query used inside a function.
     * A sub query can only be translated to the algebra once we translate the outer context and must sometimes be
     * checked for syntactic equality via Object.equals()
     */
    type SubQuery[+T]


    /**
     * Create an existential condition inside a function. The node itself is a boolean expression, but conveys that the
     * sub query must be translated in teh context of the enclosing query.
     * However, the translation of the sub query is a complex process, since it may refer to variables of the outer
     * query.
     * The context needs to be known before translating the syntax of the sub query into algebra.
     * Hence, a callback for planing the query together with a given context in relational algebra must be provided.
     */
    def existCondition[Domain, ContextDomain] (
        subQuery: SubQuery[Domain],
        planSubQueryWithContext: (SubQuery[Domain], Rep[Query[ContextDomain]],
            Rep[ContextDomain]) => Rep[Query[ContextDomain]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Boolean]

}
