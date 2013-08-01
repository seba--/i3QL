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

import idb.algebra.ir.{RelationalAlgebraIRBasicOperators, RelationalAlgebraIRBase}
import scala.virtualization.lms.common._
import idb.lms.extensions.{ExpressionUtils, FunctionsExpOptAlphaEquivalence, FunctionCreator}
import idb.algebra.base.RelationalAlgebraBasicOperators


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRNormalizeBasicOperators
    extends RelationalAlgebraIRNormalize
    with RelationalAlgebraBasicOperators
    with LiftBoolean
    with BooleanOps
    with BooleanOpsExp
    with BaseFatExp
    with EffectExp
    with TupleOpsExp
    with TupledFunctionsExp
    with FunctionsExpOptAlphaEquivalence
    with ExpressionUtils
{

    val transformer = new FunctionCreator
    {
        override val IR: RelationalAlgebraIRNormalizeBasicOperators.this.type =
            RelationalAlgebraIRNormalizeBasicOperators.this
    }

    import transformer.recreateFun

    abstract override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    ): Rep[Query[Domain]] =
        if (normalize) {
            function match {
                case Def (Lambda (f, x: Rep[Domain], body: Block[Boolean])) =>
                    body.res match {
                        case Def (BooleanOr (lhs, rhs)) =>
                            unionMax (
                                selection (relation, recreateFun (x, lhs)),
                                selection (relation, recreateFun (x, rhs))
                            )
                        case Def (BooleanAnd (lhs, Def (BooleanNegate (rhs)))) =>
                            difference (
                                selection (relation, recreateFun (x, lhs)),
                                selection (relation, recreateFun (x, rhs))
                            )
                        case Def (BooleanAnd (lhs, rhs)) =>
                            intersection (
                                selection (relation, recreateFun (x, lhs)),
                                selection (relation, recreateFun (x, rhs))
                            )
                        case _ => super.selection (relation, function)
                    }
                case _ => throw new IllegalArgumentException (function + " is not a Lambda function")
            }
        } else
        {
            super.selection (relation, function)
        }

}


