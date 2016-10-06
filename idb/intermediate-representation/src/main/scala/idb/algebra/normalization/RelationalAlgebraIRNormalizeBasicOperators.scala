/* LiceUnionBSD Style License):
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
package idb.algebra.normalization

import idb.algebra.ir.{RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRBasicOperators}
import idb.query.QueryEnvironment
import scala.virtualization.lms.common._
import idb.lms.extensions.ExpressionUtils
import idb.lms.extensions.functions.FunctionsExpDynamicLambdaAlphaEquivalence


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRNormalizeBasicOperators
    extends RelationalAlgebraNormalize
    with RelationalAlgebraIRBasicOperators
    with RelationalAlgebraIRSetTheoryOperators
    with LiftBoolean
    with BooleanOps
    with BooleanOpsExp
    with BaseFatExp
    with EffectExp
    with TupleOpsExp
    with TupledFunctionsExp
    with FunctionsExpDynamicLambdaAlphaEquivalence
    with ExpressionUtils
{

    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        if (normalize) {
            function match {
                case Def (Lambda (f, x: Rep[Domain], body: Block[Boolean])) =>
                    body.res match {
                        // σ{x ∨ y}(a) = σ{x}(a) ∪ σ{y}(a)
                        case Def (BooleanOr (lhs, rhs)) =>
                            unionMax (
                                selection (relation, dynamicLambda (x, lhs)),
                                selection (relation, dynamicLambda (x, rhs))
                            )

                        // same as above with external dependencies.
                        // Note that reifyEffects is called by dynamicLambda, thus the body is reified again if needed
                        case Def (Reify (Def (BooleanOr (lhs, rhs)), summary, deps)) =>
                            unionMax (
                                selection (relation, dynamicLambda (x, Reify (lhs, summary, deps))),
                                selection (relation, dynamicLambda (x, Reify (rhs, summary, deps)))
                            )

                        // σ{x ∧ ¬y}(a) = σ{x}(a) - σ{y}(a)
                        case Def (BooleanAnd (lhs, Def (BooleanNegate (rhs)))) =>
                            difference (
                                selection (relation, dynamicLambda (x, lhs)),
                                selection (relation, dynamicLambda (x, rhs))
                            )

                        // same as above with external dependencies.
                        // Note that reifyEffects is called by dynamicLambda, thus the body is reified again if needed
                        case Def (Reify (Def (BooleanAnd (lhs, Def (BooleanNegate (rhs)))), summary, deps)) =>
                            difference (
                                selection (relation, dynamicLambda (x, Reify (lhs, summary, deps))),
                                selection (relation, dynamicLambda (x, Reify (rhs, summary, deps)))
                            )


                        // σ{x ∧ y}(a) = σ{y}( σ{x}(a))
                        case Def (BooleanAnd (lhs, rhs)) =>
                            selection (
                                selection (relation, dynamicLambda (x, lhs)),
                                dynamicLambda (x, rhs)
                            )

                        // same as above with external dependencies.
                        // Note that reifyEffects is called by dynamicLambda, thus the body is reified again if needed
                        case Def (Reify (Def (BooleanAnd (lhs, rhs)), summary, deps)) =>
                            selection (
                                selection (relation, dynamicLambda (x, Reify (lhs, summary, deps))),
                                dynamicLambda (x, Reify (rhs, summary, deps))
                            )

                        case _ => super.selection (relation, function)
                    }
                case _ => throw new IllegalArgumentException (function.toString + " is not a Lambda function")
            }
        } else
        {
            super.selection (relation, function)
        }

}


