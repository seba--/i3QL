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
package idb.algebra.opt

import idb.algebra.ir.{RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRBasicOperators}
import idb.query.QueryEnvironment

/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptPushSetTheoryOps
    extends RelationalAlgebraIRBasicOperators
    with RelationalAlgebraIRSetTheoryOperators
{

    override def intersection[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    )(implicit env : QueryEnvironment): Rep[Query[Domain]] =
        ((relationA, relationB) match {
            // Π_f(a) ∩ Π_f(b) => Π_f(a ∩ b)
            case (Def (Projection (a, fa)), Def (Projection (b, fb))) if fa == fb =>
                projection (intersection (a, b)(domainOf (a), env), fa)

            //  (a × b) ∩ (c × d) => (a ∩ c) × (b ∩ d)
            case (Def (CrossProduct (a, b)), Def (CrossProduct (c, d))) =>
                crossProduct (
                    intersection (a, c)(domainOf (a), env),
                    intersection (b, d)(domainOf (b), env)
                )

            //  (a × b) ∩ (c ⋈ d) => (a ∩ c) ⋈ (b ∩ d)
            case (Def (CrossProduct (a, b)), Def (EquiJoin (c, d, l))) =>
                equiJoin (
                    intersection (a, c)(domainOf (a), env),
                    intersection (b, d)(domainOf (b), env),
                    l
                )

            //  (a ⋈ b) ∩ (c × d) => (a ∩ c) ⋈ (b ∩ d)
            case (Def (EquiJoin (a, b, l)), Def (CrossProduct (c, d))) =>
                equiJoin (
                    intersection (a, c)(domainOf (a), env),
                    intersection (b, d)(domainOf (b), env),
                    l
                )

            //  (a ⋈ b) ∩ (c ⋈ d) => (a ∩ c) ⋈ (b ∩ d)
            case (Def (EquiJoin (a, b, l1)), Def (EquiJoin (c, d, l2))) =>
                equiJoin (
                    intersection (a, c)(domainOf (a), env),
                    intersection (b, d)(domainOf (b), env),
                    l1 ::: l2
                )

            case _ => super.intersection (relationA, relationB)


        }).asInstanceOf[Rep[Query[Domain]]]


    override def unionMax[DomainA <: Range : Manifest, DomainB <: Range : Manifest, Range: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    )(implicit env : QueryEnvironment): Rep[Query[Range]] =
        ((relationA, relationB) match {
            /*
            // Π_f(a) ∪ Π_f(b) => Π_f(a ∪ b)
            case (Def (Projection (a, fa)), Def (Projection (b, fb))) if fa == fb =>
                projection (unionMax (a, b)(domainOf (a), domainOf (b), manifest[Range].asInstanceOf[Manifest[Any]]),
                    fa)
             */


            //  (a × b) ∪ (c × d) => (a ∪ c) × (b ∪ d)
            case (Def (CrossProduct (a, b)), Def (CrossProduct (c, d))) =>
                crossProduct (
                    unionMax (a, c)(domainOf (a), domainOf (b), manifest[Range].asInstanceOf[Manifest[Any]], env),
                    unionMax (b, d)(domainOf (a), domainOf (b), manifest[Range].asInstanceOf[Manifest[Any]], env)
                )

            //  (a × b) ∪ (c ⋈ d) => (a ∪ c) ⋈ (b ∪ d)
            case (Def (CrossProduct (a, b)), Def (EquiJoin (c, d, l))) =>
                equiJoin (
                    unionMax (a, c)(domainOf (a), domainOf (b), manifest[Range].asInstanceOf[Manifest[Any]], env),
                    unionMax (b, d)(domainOf (a), domainOf (b), manifest[Range].asInstanceOf[Manifest[Any]], env),
                    l
                )

            //  (a ⋈ b) ∪ (c × d) => (a ∪ c) ⋈ (b ∪ d)
            case (Def (EquiJoin (a, b, l)), Def (CrossProduct (c, d))) =>
                equiJoin (
                    unionMax (a, c)(domainOf (a), domainOf (b), manifest[Range].asInstanceOf[Manifest[Any]], env),
                    unionMax (b, d)(domainOf (a), domainOf (b), manifest[Range].asInstanceOf[Manifest[Any]], env),
                    l
                )

            //  (a ⋈ b) ∪ (c ⋈ d) => (a ∪ c) ⋈ (b ∪ d)
            case (Def (EquiJoin (a, b, l1)), Def (EquiJoin (c, d, l2))) =>
                equiJoin (
                    unionMax (a, c)(domainOf (a), domainOf (b), manifest[Range].asInstanceOf[Manifest[Any]], env),
                    unionMax (b, d)(domainOf (a), domainOf (b), manifest[Range].asInstanceOf[Manifest[Any]], env),
                    l1 ::: l2
                )



            //  (a - b) ∪ (c - d) => (a ∪ c) - (b ∪ d)
            case (Def (Difference (a, b)), Def (Difference (c, d))) =>
                difference (
                    unionMax (a, b),
                    unionMax (c, d)
                )

            //  a ∪ (b - c) => (a ∪ b) - (c - a)
            case (a, Def (Difference (b, c))) =>
                difference (
                    unionMax (a, b),
                    difference (c, a)
                )


            //  (b - c) ∪ a => (b ∪ a) - (c - a)
            case (Def (Difference (b, c)), a)=>
                difference (
                    unionMax (b, a),
                    difference (c, a)
                )


            /*
            //  (a - b) ∪ ((c - d) ∪ e) => ((a ∪ c) - (b ∪ d)) ∪ e
            case (Def (Difference (a, b)), Def (UnionMax (Def (Difference (c, d)), e))) =>
                unionMax (
                    difference (
                        unionMax (a, b),
                        unionMax (c, d)
                    ),
                    e
                )
            */

/*
            //   Π_f(a) ∪ Π_f(b) => Π_f(a ∪ b)
            case (Def (Projection (ra, fa)), Def (Projection (rb, fb))) if fa == fb =>
                projection (
                    unionMax (ra, rb),
                    fa
                )


            //   Π_f(a) ∪ (Π_f(b) ∪ c) => Π_f(a ∪ b) ∪ c
            case (Def (Projection (ra, fa)), Def (UnionMax (Def (Projection (rb, fb)), rc))) if fa == fb =>
                unionMax (
                    projection (
                        unionMax (ra, rb),
                        fa
                    ),
                    rc
                )

            //   Π_f(a) ∪ (b ∪ Π_f(c)) => Π_f(a ∪ c) ∪ b
            case (Def (Projection (ra, fa)), Def (UnionMax (rb, Def (Projection (rc, fb))))) if fa == fb =>
                unionMax (
                    projection (
                        unionMax (ra, rc),
                        fa
                    ),
                    rb
                )
*/

            case _ => super.unionMax (relationA, relationB)

        }).asInstanceOf[Rep[Query[Range]]]
}
