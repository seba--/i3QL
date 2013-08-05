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

import scala.virtualization.lms.common._
import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.lms.extensions.{FunctionsExpOptAlphaEquivalence, FunctionCreator, FunctionUtils, ExpressionUtils}

/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptPushSelect
    extends RelationalAlgebraIRBasicOperators
    with BaseFatExp
    with LiftBoolean
    with BooleanOpsExp
    with TupleOpsExp
    with TupledFunctionsExp
    with EqualExp
    with ExpressionUtils
    with FunctionUtils
    with FunctionCreator
    with FunctionsExpOptAlphaEquivalence
{

    /**
     * Pushing selection down over other operations
     */
    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    ): Rep[Query[Domain]] = {
        relation match {
            // pushing over projections
            case Def (Projection (r, f)) => {
                val pushedFunction = fun ((x: Exp[Any]) => function (f (x)))(
                    f.tp.typeArguments (0).asInstanceOf[Manifest[Any]], manifest[Boolean])
                projection (selection (r, pushedFunction), f)
            }
            // pushing selections that only use their arguments partially over selections that need all arguments
            case Def (Selection (r, f)) if !freeVars (function).isEmpty && freeVars (f).isEmpty =>
                super.selection (super.selection (relation, f), function)

            // pushing selections that use equalities over selections that do not
            case Def (Selection (r, f)) if isDisjunctiveParameterEquality(function) && !isDisjunctiveParameterEquality(f) =>
                super.selection (super.selection (relation, f), function)


            case Def (CrossProduct (a, b)) => {
                pushedFunctions (function) match {
                    case (None, None) =>
                        super.selection (relation, function)
                    case (Some (f), None) =>
                        crossProduct (selection (a, f), b).asInstanceOf[Rep[Query[Domain]]]
                    case (None, Some (f)) =>
                        crossProduct (a, selection (b, f)).asInstanceOf[Rep[Query[Domain]]]
                    case (Some (fa), Some (fb)) =>
                        crossProduct (selection (a, fa), selection (b, fb)).asInstanceOf[Rep[Query[Domain]]]
                }
            }
            case _ =>
                super.selection (relation, function)
        }
    }

    private def pushedFunctions[Domain: Manifest] (
        function: Rep[Domain => Boolean]
    ): (Option[Rep[Any => Boolean]], Option[Rep[Any => Boolean]]) = {
        val freeV = freeVars (function)
        if (freeV.isEmpty) {
            return (None, None)
        }
        val functionBody = body (function)
        val functionParameters = parameters (function)
        if (freeV.size == 2) {
            return (
                Some (recreateFunRepDynamic (functionParameters (0), functionBody)),
                Some (recreateFunRepDynamic (functionParameters (1), functionBody))
                )
        }

        functionParameters.indexOf (freeV (0)) match {
            case 0 => (None, Some (recreateFunRepDynamic (functionParameters (1), functionBody)))
            case 1 => (Some (recreateFunRepDynamic (functionParameters (0), functionBody)), None)
        }

    }

}
