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
package idb.algebra.fusion

import scala.virtualization.lms.common._
import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.algebra.normalization.RelationalAlgebraIRNormalize
import idb.lms.extensions.FunctionUtils

/**
 *
 * TODO could check that the functions are pure (i.e., side-effect free), an only then do shortcut evaluation
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRFuseBasicOperators
    extends RelationalAlgebraIRBasicOperators
    with RelationalAlgebraIRNormalize
    with LiftBoolean
    with BooleanOps
    with BooleanOpsExp
    with EffectExp
    with FunctionsExp
    with FunctionUtils
{

    /**
     * create a new conjunction.
     * Types are checked dynamically to conform to Domain.
     *
     */
    private def createConjunction[A, B, Domain: Manifest] (
        fa: Rep[A => Boolean],
        fb: Rep[B => Boolean]
    ): Rep[Domain => Boolean] = {
        val mDomain = implicitly[Manifest[Domain]]
        if (!(fa.tp.typeArguments (0) >:> mDomain)) {
            throw new IllegalArgumentException (fa.tp.typeArguments (0) + " must conform to " + mDomain)
        } else if (!(fb.tp.typeArguments (0) >:> mDomain)) {
            throw new IllegalArgumentException (fb.tp.typeArguments (0) + " must conform to " + mDomain)
        }

        val faUnsafe = fa.asInstanceOf[Rep[Domain => Boolean]]
        val fbUnsafe = fb.asInstanceOf[Rep[Domain => Boolean]]

        fun ((x: Rep[Domain]) => faUnsafe (x) && fbUnsafe (x))(mDomain, manifest[Boolean])
    }


    /**
     * create a new conjunction.
     * Types are checked dynamically to conform to Domain.
     *
     */
    private def createDisjunction[A, B, Domain: Manifest] (
        fa: Rep[A => Boolean],
        fb: Rep[B => Boolean]
    ): Rep[Domain => Boolean] = {
        val mDomain = implicitly[Manifest[Domain]]
        if (!(fa.tp.typeArguments (0) >:> mDomain)) {
            throw new IllegalArgumentException (fa.tp.typeArguments (0) + " must conform to " + mDomain)
        } else if (!(fb.tp.typeArguments (0) >:> mDomain)) {
            throw new IllegalArgumentException (fb.tp.typeArguments (0) + " must conform to " + mDomain)
        }

        val faUnsafe = fa.asInstanceOf[Rep[Domain => Boolean]]
        val fbUnsafe = fb.asInstanceOf[Rep[Domain => Boolean]]

        fun ((x: Rep[Domain]) => faUnsafe (x) || fbUnsafe (x))(mDomain, manifest[Boolean])
    }

    /**
     * Fusion of projection operations
     */
    override def projection[Domain: Manifest, Range: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Range]
    ): Rep[Query[Range]] =
        relation match {
            case Def (Projection (r, f)) =>
                val mPrevDomainUnsafe = f.tp.typeArguments (0).asInstanceOf[Manifest[Any]]
                val mRangeUnsafe = function.tp.typeArguments (1).asInstanceOf[Manifest[Range]]
                projection (r, fun ((x: Rep[_]) => function (f (x)))(parameterType(f), mRangeUnsafe))
            case _ =>
                super.projection (relation, function)
        }


    /**
     * Fusion of selection operations
     */
    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    ): Rep[Query[Domain]] =
        relation match {
            case Def (Selection (r, f)) => {
                withoutNormalization (
                    selection (r, createConjunction (f, function))
                )
            }
            case _ =>
                super.selection (relation, function)
        }


    override def unionMax[DomainA <: Range : Manifest, DomainB <: Range : Manifest, Range: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    ): Rep[Query[Range]] =
        (relationA, relationB) match {
            case (Def (Selection (a, fa)), Def (Selection (b, fb))) if a == b =>
                withoutNormalization (
                    selection (
                        a.asInstanceOf[Rep[Query[Range]]],
                        createDisjunction (fa, fb)(parameterType(fa))
                    )
                )
            case _ =>
                super.unionMax (relationA, relationB)
        }

    override def intersection[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    ): Rep[Query[Domain]] =
        (relationA, relationB) match {
            case (Def (Selection (a, fa)), Def (Selection (b, fb))) if a == b =>
                withoutNormalization (
                    selection (
                        a,
                        createConjunction (fa, fb)(parameterType(fa))
                    )
                )
/*
            case (Def (CrossProduct (Def (Selection (a, fa)), b)), Def (CrossProduct (Def (Selection (c, fc)), d)))
                if a == c =>
                withoutNormalization (
                    crossProduct (
                        selection (a, createConjunction (fa, fc)(parameterType (fa))),
                        intersection (b, d)
                    ).asInstanceOf[Rep[Query[Domain]]]
                )

            case (Def (CrossProduct (Def (Selection (a, fa)), b)), Def (CrossProduct (c, Def (Selection (d, fd)))))
                if a == c && b == d =>
                withoutNormalization (
                    crossProduct (selection (c, fa), selection (b, fd)).asInstanceOf[Rep[Query[Domain]]]
                )
*/
            case _ =>
                super.intersection (relationA, relationB)
        }


    override def difference[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    ): Rep[Query[Domain]] =
        (relationA, relationB) match {
            case (Def (Selection (a, fa)), Def (Selection (b, fb))) if a == b =>
                withoutNormalization (
                    selection (
                        a,
                        createConjunction (fa, fb)(parameterType(fa))
                    )
                )
            case _ =>
                super.difference (relationA, relationB)
        }
}
