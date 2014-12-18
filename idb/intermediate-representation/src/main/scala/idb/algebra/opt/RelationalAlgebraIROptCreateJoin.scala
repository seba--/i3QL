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

import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.lms.extensions.FunctionUtils
import scala.virtualization.lms.common.{EqualExp, TupledFunctionsExp}
import idb.lms.extensions.functions.FunctionsExpDynamicLambdaAlphaEquivalence

/**
 *
 * @author Ralf Mitschke
 *
 */

trait RelationalAlgebraIROptCreateJoin
    extends RelationalAlgebraIRBasicOperators
    with TupledFunctionsExp
    with EqualExp
    with FunctionUtils
    with FunctionsExpDynamicLambdaAlphaEquivalence
{
    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    ): Rep[Query[Domain]] = {
        (relation match {
            // rewrite a selection with a function of the form (a, b) => exprOf(a) == exprOf(b) into a join
			case Def(c@CrossProduct(a, b)) if isDisjunctiveParameterEquality(function)(c.mDomA, c.mDomB) => {
				equiJoin(a, b, List(createEqualityFunctions(function)(c.mDomA, c.mDomB)))(c.mDomA, c.mDomB)
			}

			// add further equality tests to the join
			case Def(c@EquiJoin(a, b, xs)) if isDisjunctiveParameterEquality(function)(c.mDomA, c.mDomB) =>
				equiJoin(a, b, xs ::: List(createEqualityFunctions(function)(c.mDomA, c.mDomB)))(c.mDomA, c.mDomB)

            case _ => super.selection (relation, function)
        }).asInstanceOf[Rep[Query[Domain]]]
    }




	def createEqualityFunctions[A,B,C](function: Exp[A => Boolean])(implicit mDomX : Manifest[B], mDomY : Manifest[C]): (Exp[Any => Boolean], Exp[Any => Boolean]) = {
		val params = parameters(function)
		val b = body(function)

		if (params.size == 1 && isTuple2Manifest(params(0).tp)) {
			val t = params(0).asInstanceOf[Exp[Tuple2[B,C]]]
			val tupledParams = scala.collection.immutable.Set(t._1, t._2)

			b match {
				case Def(Equal(lhs: Exp[Boolean@unchecked], rhs: Exp[Boolean@unchecked])) => {
					val usedByLeft = findSyms(lhs)(tupledParams)
					val usedByRight = findSyms(rhs)(tupledParams)
					if (usedByLeft.size != 1 || usedByRight.size != 1 && usedByLeft == usedByRight) {
						throw new java.lang.IllegalArgumentException(
							"Expected equality that separates left and right parameter in function " + function.toString)
					}

					val l = tupledParams.toList

					val x = l(0)
					val y = l(1)
					if (usedByLeft == Set(x)) {
						return (dynamicLambda(x, lhs), dynamicLambda(y, rhs))
					}
					else {
						return (dynamicLambda(x, rhs), dynamicLambda(y, lhs))
					}
				}
				case _ => throw new java.lang.IllegalArgumentException("Expected equality in function " + function.toString)
			}
		} else if (params.size == 2) {
			 b match {
				case Def(Equal(lhs: Exp[Boolean@unchecked], rhs: Exp[Boolean@unchecked])) => {
					val usedByLeft = findSyms(lhs)(params.toSet)
					val usedByRight = findSyms(rhs)(params.toSet)
					if (usedByLeft.size != 1 || usedByRight.size != 1 && usedByLeft == usedByRight) {
						throw new java.lang.IllegalArgumentException(
							"Expected equality that separates left and right parameter in function " + function.toString)
					}
					val x = params(0)
					val y = params(1)
					if (usedByLeft == Set(x)) {
						return (dynamicLambda(x, lhs), dynamicLambda(y, rhs))
					}
					else {
						return (dynamicLambda(x, rhs), dynamicLambda(y, lhs))
					}
				}
				case _ => throw new java.lang.IllegalArgumentException("Expected equality in function " + function.toString)
			}
		}

		throw new java.lang.IllegalArgumentException("Expected two parameters or Tuple2 parameter for function " + function.toString)

	}
}
