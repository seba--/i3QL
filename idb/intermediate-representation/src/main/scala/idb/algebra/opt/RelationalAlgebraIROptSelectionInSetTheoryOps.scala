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
import idb.algebra.normalization.RelationalAlgebraNormalize
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.functions.FunctionsExpDynamicLambdaAlphaEquivalence
import idb.query.QueryEnvironment

/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptSelectionInSetTheoryOps
    extends RelationalAlgebraIRBasicOperators
    with RelationalAlgebraIRSetTheoryOperators
    with FunctionUtils
    with FunctionsExpDynamicLambdaAlphaEquivalence
    with RelationalAlgebraNormalize
{

    override def intersection[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    )(implicit env : QueryEnvironment): Rep[Query[Domain]] =
        (relationA, relationB) match {
            case (Def (Selection (_, fa)), Def (Selection (_, fb))) =>
                super.intersection (
                    selection (relationA, fb),
                    selection (relationB, fa)
                )

            case (Def (Selection (_, fa)), _) =>
                super.intersection (
                    relationA,
                    selection (relationB, fa)
                )

            case (_, Def (Selection (_, fb))) =>
                super.intersection (
                    selection (relationA, fb),
                    relationB
                )

            case _ => super.intersection (relationA, relationB)
        }

    override def difference[Domain: Manifest] (
        relationA: Rep[Query[Domain]],
        relationB: Rep[Query[Domain]]
    )(implicit env : QueryEnvironment): Rep[Query[Domain]] =
        relationA match {
            case Def (Selection (_, fa)) =>
                withoutNormalization (
                    super.difference (relationA, selection (relationB, fa))
                )

            case _ =>
                super.difference (relationA, relationB)
        }

}
