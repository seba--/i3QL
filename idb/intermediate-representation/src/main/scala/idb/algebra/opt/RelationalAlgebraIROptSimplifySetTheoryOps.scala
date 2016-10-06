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
 * Simplification rules remove operators that reduce to trivial meanings.
 * For example: a ∩ a = a
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptSimplifySetTheoryOps
    extends RelationalAlgebraIRSetTheoryOperators
    with RelationalAlgebraIRBasicOperators
{

    override def intersection[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        (relationA, relationB) match {
            // a ∩ a = a
            case (a, b) if a == b =>
                a

            // σ(a) ∩ a = σ(a)
            case (Def (Selection (a, f)), b) if a == b =>
                selection (a, f)

            //  a ∩ σ(a) = σ(a)
            case (a, Def (Selection (b, f))) if a == b =>
                selection (a, f)

            //  σ(a) ∩ σ(a) = σ(σ(a))
            case (Def (Selection (a, fa)), Def (Selection (b, fb))) if a == b =>
                selection (selection (a, fa), fb)

            case _ => super.intersection (relationA, relationB)

        }

    override def unionMax[DomainA <: Range : Manifest, DomainB <: Range : Manifest, Range: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] =
        ((relationA, relationB) match {

            // a ∪ a = a
            case (a, b) if a == b =>
                a

            // (a ∪ b) ∪ a = (a ∪ b)
            case (Def (UnionMax (a, b)), x) if x == a =>
                unionMax (a, b)

            // (a ∪ b) ∪ b = (a ∪ b)
            case (Def (UnionMax (a, b)), x) if x == b =>
                unionMax (a, b)

            // a ∪ (a ∪ b) = (a ∪ b)
            case (x, Def (UnionMax (a, b))) if x == a =>
                unionMax (a, b)

            // b ∪ (a ∪ b) = (a ∪ b)
            case (x, Def (UnionMax (a, b))) if x == b =>
                unionMax (a, b)

            // σ(a) ∪ a = σ(a)
            case (Def (Selection (a, f)), b) if a == b =>
                selection (a, f)

            //  a ∪ σ(a) = σ(a)
            case (a, Def (Selection (b, f))) if a == b =>
                selection (b, f)

            case _ => super.unionMax (relationA, relationB)

        }).asInstanceOf[Rep[Query[Range]]]


    override def difference[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        (relationA, relationB) match {

            // (a - b) - (c - d) = a - ((b ∪ c) - d)
            case (Def (Difference (a, b)), Def (Difference (c, d))) =>
                difference (
                    a,
                    difference (
                        unionMax (b, c),
                        d
                    )
                )

            case _ => super.difference (relationA, relationB)
        }
}


