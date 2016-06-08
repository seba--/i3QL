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
import idb.lms.extensions.functions.{TupledFunctionsExpDynamicLambda, FunctionsExpDynamicLambdaAlphaEquivalence}
import idb.lms.extensions.{FunctionUtils, ExpressionUtils}
import idb.query.QueryEnvironment
import scala.virtualization.lms.common._

/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptPushSelection
    extends RelationalAlgebraIRBasicOperators
    with RelationalAlgebraIRSetTheoryOperators
    with BaseFatExp
    with LiftBoolean
    with BooleanOpsExp
    with TupleOpsExp
    with TupledFunctionsExpDynamicLambda
    with EqualExp
    with ExpressionUtils
    with FunctionUtils
    with FunctionsExpDynamicLambdaAlphaEquivalence
{

    /**
     * Pushing selection down over other operations
     */
    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] = {
        (relation match {
            // pushing over projections
            case Def (Projection (r, f)) => {
                val pushedFunction = fun ((x: Exp[Any]) => function (f (x)))(
                    parameterType (f), manifest[Boolean])
                projection (selection (r, pushedFunction)(domainOf (r), queryEnvironment), f)
            }
            // pushing selections that only use their arguments partially over selections that need all arguments
            case Def (Selection (r, f)) if !freeVars (function).isEmpty && freeVars (f).isEmpty =>
                selection (selection (r, function)(exactDomainOf (relation), queryEnvironment), f)

            // pushing selections that use equalities over selections that do not
            case Def (Selection (r, f)) if isDisjunctiveParameterEquality (
                function) && !isDisjunctiveParameterEquality (f) =>
                selection (selection (r, function)(exactDomainOf (relation), queryEnvironment), f)

            case Def (CrossProduct (a, b)) => {
                pushedOverBinaryOperator (function) match {
                    case (None, None) =>
                        super.selection (relation, function)
                    case (Some (f), None) =>
                        crossProduct (
                            selection (a, f)(parameterType(f), queryEnvironment),
                            b
                        )(domainOf (a), domainOf (b), queryEnvironment)

                    case (None, Some (f)) =>
                        crossProduct (
                            a,
                            selection (b, f)(parameterType(f), queryEnvironment)
                        )(domainOf (a), domainOf (b), queryEnvironment)

                    case (Some (fa), Some (fb)) =>
                        crossProduct (
                            selection (a, fa)(parameterType(fa), queryEnvironment),
                            selection (b, fb)(parameterType(fb), queryEnvironment)
                        )(domainOf (a), domainOf (b), queryEnvironment)
                }
            }

            case Def (EquiJoin (a, b, l)) => {
                pushedOverBinaryOperator (function) match {
                    case (None, None) =>
                        super.selection (relation, function)

                    case (Some (f), None) =>
                        equiJoin (
                            selection (a, f)(parameterType(f), queryEnvironment),
                            b,
                            l
                        )(domainOf (a), domainOf (b), queryEnvironment)

                    case (None, Some (f)) =>
                        equiJoin (
                            a,
                            selection (b, f)(parameterType(f), queryEnvironment),
                            l
                        )(domainOf (a), domainOf (b), queryEnvironment)

                    case (Some (fa), Some (fb)) =>
                        equiJoin (
                            selection (a, fa)(parameterType(fa), queryEnvironment),
                            selection (b, fb)(parameterType(fb), queryEnvironment),
                            l
                        )(domainOf (a), domainOf (b), queryEnvironment)
                }
            }

            case Def (Difference (a, b)) =>
                difference (
                    selection (a, function),
                    selection (b, function)
                )

            case Def (UnionMax (a, b)) =>
                unionMax (
                    selection (a, function),
                    selection (b, function)
                )

            case _ =>
                super.selection (relation, function)

        }).asInstanceOf[Rep[Query[Domain]]]
    }


    private def pushedOverBinaryOperator[Domain: Manifest] (
        function: Rep[Domain => Boolean]
    ): (Option[Rep[Any => Boolean]], Option[Rep[Any => Boolean]]) = {
        val symsInQuestion =
            parameter (function) match {
                case UnboxedTuple (scala.List (a, b)) =>
                    scala.List (a, b)

                // in case we have a function f(x:Tuple2[A,B]) we are looking for usage of only x._1 or x._2
                case s: Sym[Domain@unchecked] if isTuple2Manifest (s.tp) =>
                    parametersAsList (unbox (s))

                case e =>
                    throw new IllegalArgumentException (
                        "Expected a tupled value type to push over binary operator. Found: " + e.tp)
            }

        val freeV = unusedVars (function, symsInQuestion)
        if (freeV.isEmpty) {
            return (None, None)
        }


        // TODO Using symsInQuestion(i) below as parameter means that we can have x._1 and x._2 as a parameter.
        // We could change that.
        // However, there is no problem, since there is always a variable (i.e., a Sym) for x._1, i.e., y = x._1
        // Thus function application will replace the whole y.

        val functionBody = body (function)

		val sym0 = symsInQuestion (0)
		val sym1 = symsInQuestion (1)


        if (freeV.size == 2) {
			val f1 = dynamicLambda (sym0, functionBody)
			val f2 = dynamicLambda (sym1, functionBody)

           return (Some (f1), Some (f2))
        }

        symsInQuestion.indexOf (freeV (0)) match {
            case 0 =>
				val f = dynamicLambda (sym1, functionBody)
				return (None, Some (f))
            case 1 =>
				val f = dynamicLambda (sym0, functionBody)
				return (Some (f), None)
        }

		return (None, None)
    }



}
