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

import idb.algebra.ir.{RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRBasicOperators}
import idb.algebra.normalization.RelationalAlgebraNormalize
import idb.lms.extensions.FunctionUtils
import idb.query.QueryEnvironment

/**
 *
 * TODO could check that the functions are pure (i.e., side-effect free), an only then do shortcut evaluation
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRFuseSetTheoryOperators
    extends RelationalAlgebraIRBasicOperators
    with RelationalAlgebraIRSetTheoryOperators
    with RelationalAlgebraNormalize
    with FunctionUtils
{


    override def unionMax[DomainA <: Range : Manifest, DomainB <: Range : Manifest, Range: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Range]] =
        (relationA, relationB) match {
            case (Def (Selection (ra, fa)), Def (Selection (rb, fb))) if ra == rb =>
                withoutNormalization (
                    selection (
                        ra.asInstanceOf[Rep[Query[Range]]],
                        createDisjunction (fa, fb)(parameterType (fa))
                    )
                )


            case (Def (Selection (ra, fa)), Def (UnionMax (Def (Selection (rb, fb)), rx))) if ra == rb =>
                unionMax (
                    withoutNormalization (
                        selection (
                            ra.asInstanceOf[Rep[Query[Range]]],
                            createDisjunction (fa, fb)(parameterType (fa))
                        )
                    ),
                    rx
                )

            case _ =>
                super.unionMax (relationA, relationB)
        }

    override def intersection[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        (relationA, relationB) match {
            case (Def (Selection (ra, fa)), Def (Selection (rb, fb))) if ra == rb =>
                withoutNormalization (
                    selection (
                        ra,
                        createConjunction (fa, fb)(parameterType(fa))
                    )
                )

            case _ =>
                super.intersection (relationA, relationB)
        }


    override def difference[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        (relationA, relationB) match {
            case (Def (Selection (ra, fa)), Def (Selection (rb, fb))) if ra == rb =>
                withoutNormalization (
                    selection (
                        ra,
                        createConjunction (fa,
                            fun ((x: Rep[Domain]) => !fb (x))
                        )(parameterType(fa))
                    )
                )
            case _ =>
                super.difference (relationA, relationB)
        }
}
