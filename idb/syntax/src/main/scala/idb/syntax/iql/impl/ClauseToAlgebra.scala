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
package idb.syntax.iql.impl

import idb.syntax.iql.IR._
import idb.syntax.iql._
import scala.virtualization.lms.common.{ForwardTransformer, RecursiveTransformer, WorklistTransformer}

/**
 *
 * @author Ralf Mitschke
 *
 */

object ClauseToAlgebra
    extends ForwardTransformer
{
    val IR = idb.syntax.iql.IR

    def apply[Range: Manifest] (query: IQL_QUERY[Range]): Rep[Query[Range]] =
        query match {
            case FromClause1 (relation, SelectClause1 (project)) =>
                projection (relation, project)

            case FromClause2 (relationA, relationB, SelectClause2 (project)) =>
                projection (crossProduct (relationA, relationB), project)

            case WhereClause1 (predicate, FromClause1 (relation, SelectClause1 (project))) =>
                projection (selection (relation, predicate), project)

            case WhereClause2 (predicate, FromClause2 (relationA, relationB, SelectClause2 (project))) => {
                val foo = discernPredicates2 (predicate, relationA, relationB)
                projection (foo, project)
            }
        }


    def discernPredicates2[DomainA: Manifest, DomainB: Manifest] (
        predicate: (Rep[DomainA], Rep[DomainB]) => Rep[Boolean],
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    ): Rep[Query[(DomainA, DomainB)]] = {
        val a = fresh[DomainA]
        val b = fresh[DomainB]
        val body = predicate (a, b)
        implicit val searchParams: Set[Sym[Any]] = Predef.Set (a, b)

        getFunction2 (a, b, body) match {
            case (Some (predA), Some (predB)) =>
                crossProduct (selection (relationA, predA), selection (relationB, predB))
            case (Some (predA), None) =>
                crossProduct (selection (relationA, predA), relationB)
            case (None, Some (predB)) =>
                crossProduct (relationA, selection (relationB, predB))
            case (None, None) =>
                crossProduct (relationA, relationB)
        }
    }

    def getFunction2[DomainA: Manifest, DomainB: Manifest] (
        a: Sym[DomainA],
        b: Sym[DomainB],
        body: Rep[Boolean]
    )(
        implicit allParams: Set[Sym[Any]]
    ): (Option[Rep[DomainA] => Rep[Boolean]], Option[Rep[DomainB] => Rep[Boolean]]) = {
        val sa: Predef.Set[Sym[Any]] = Predef.Set (a)
        val sb: Predef.Set[Sym[Any]] = Predef.Set (b)
        findSyms (body) match {
            case `sa` =>
                return (Some ((x: Rep[DomainA]) => {
                    //register (a)(x)
                    subst = Predef.Map(a -> x)
                    //runOnce (reifyEffects (body)).res
                    transformBlock(reifyEffects (body)).res
                }), None)
            case `sb` =>
                return (None, Some ((x: Rep[DomainB]) => {
                    //register (b)(x)
                    subst = Predef.Map(b -> x)
                    //runOnce (reifyEffects (body)).res
                    transformBlock(reifyEffects (body)).res
                }))
            case _ => // do nothing
        }

        // there are multiple parameter syms yet in the body
        body match {
            // match conjunctions that can be used before a join
            case Def (BooleanAnd (lhs, rhs)) =>
                return (None, None)
            // match a join condition
            case Def (Equal (lhs, rhs)) =>
                return (None, None)
        }
    }

}
