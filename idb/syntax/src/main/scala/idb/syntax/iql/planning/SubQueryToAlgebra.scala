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
package idb.syntax.iql.planning

import idb.syntax.iql._
import idb.syntax.iql.impl._

/**
 *
 * @author Ralf Mitschke
 *
 */

object SubQueryToAlgebra
{

    val IR = idb.syntax.iql.IR

    import IR._

    private def applyDistinct[Domain: Manifest] (query: Rep[Query[Domain]], asDistinct: Boolean) =
        asDistinct match {
            case true => duplicateElimination (query)
            case false => query
        }


    def apply[Select : Manifest, Domain <: GroupDomain : Manifest, GroupDomain : Manifest, GroupRange <: Select : Manifest, Range : Manifest, ContextRange: Manifest] (
        subQuery: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range],
        context: Rep[Query[ContextRange]],
        contextParameter: Rep[ContextRange]
    ): Rep[Query[ContextRange]] =
        subQuery match {
            // This clause is definitely not correlated to the context
            case FromClause1 (relation, SelectClause1 (_, asDistinct)) =>
                applyDistinct (
                    projection (
                        crossProduct (context, relation),
                        (ctx: Rep[ContextRange], dom: Rep[_]) => ctx //TODO Is Rep[_] ok?
                    ),
                    asDistinct
                )


            // This clause might be correlated.
            // But, the clause could also just filter some elements without referring to the context
			//TODO Re-enable subquery to algebra
          /*  case WhereClause1 (predicate, FromClause1 (relation, SelectClause1 (_, asDistinct))) =>
                applyDistinct (
                    projection (
                        selection (
                        crossProduct (context, relation),
                        {
                            val ctxFun = dynamicLambda (contextParameter,
                                dynamicLambda (parameter (predicate), body (predicate)))
                            fun (
                                (ctx: Rep[ContextRange], cur: Rep[Domain]) => {
                                    val app1 = ctxFun (ctx)
                                    val app2 = app1 (cur)
                                    app2
                                }
                            )
                        }
                        ),
                        (ctx: Rep[ContextRange], dom: Rep[Select]) => ctx
                    ),
                    asDistinct
                )  */
        }

    def apply[SelectA: Manifest, SelectB: Manifest, DomainA <: SelectA : Manifest, DomainB <: SelectB : Manifest,
    Range: Manifest, ContextRange: Manifest] (
        subQuery: IQL_QUERY_2[SelectA, SelectB, DomainA, DomainB, Range],
        context: Rep[Query[ContextRange]]
    ): Rep[Query[Range]] =
        subQuery match {
            case _ => throw new UnsupportedOperationException
        }

}
