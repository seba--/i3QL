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
package idb.algebra.ir

import idb.algebra.base.RelationalAlgebraSubQueries
import idb.lms.extensions.equivalence.FunctionsExpAlphaEquivalence
import idb.query.QueryEnvironment

/**
 *
 * @author Ralf Mitschke
 *
 */

trait RelationalAlgebraIRSubQueries
    extends RelationalAlgebraSubQueries
    with FunctionsExpAlphaEquivalence
{

    case class ExistsCondition[Domain, ContextDomain] (
        subQuery: SubQuery[Domain]
    ) extends Def[Boolean]
    {

        def createSubQueryWithContext (
            context: Rep[Query[ContextDomain]],
            parameter: Rep[ContextDomain]
        ): Rep[Query[ContextDomain]] =
            contextualQueryPlan (subQuery, context, parameter)

        var contextualQueryPlan: (SubQuery[Domain], Rep[Query[ContextDomain]],
            Rep[ContextDomain]) => Rep[Query[ContextDomain]] =
            null

        def this (
            subQuery: SubQuery[Domain],
            contextualQueryPlan: (SubQuery[Domain], Rep[Query[ContextDomain]],
                Rep[ContextDomain]) => Rep[Query[ContextDomain]]
        ) {
            this (subQuery)
            this.contextualQueryPlan = contextualQueryPlan
        }

    }

    def existCondition[Domain, ContextDomain] (
        subQuery: SubQuery[Domain],
        planSubQueryWithContext: (SubQuery[Domain], Rep[Query[ContextDomain]],
            Rep[ContextDomain]) => Rep[Query[ContextDomain]]
    )(implicit env : QueryEnvironment): Rep[Boolean] =
        new ExistsCondition (subQuery, planSubQueryWithContext)

    override def isEquivalent[A, B] (a: Exp[A], b: Exp[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (Def (ExistsCondition (subQueryA)), Def (ExistsCondition (subQueryB))) => subQueryA == subQueryB
            case _ => super.isEquivalent(a, b)
        }

}
