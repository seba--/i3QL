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

import idb.algebra.ir.{RelationalAlgebraIRRecursiveOperators, RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}
import scala.virtualization.lms.common._
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

	def printPrivRel(q : Rep[Query[_]]) = q match {
		case Def(d) => println(d)
		case _ => println(q)
	}



    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    ): Rep[Query[Domain]] = {
		print("selection : ")
		printPrivRel(relation)



        (relation match {
            // rewrite a selection with a function of the form (a, b) => exprOf(a) == exprOf(b) into a join
            case Def (CrossProduct (a, b)) if isDisjunctiveParameterEquality (function) => {
				println("CP found")
				equiJoin(a, b, List(createEqualityFunctions(function)))(domainOf(a), domainOf(b))
			}

            // add further equality tests to the join
            case Def (EquiJoin (a, b, xs)) if isDisjunctiveParameterEquality (function) =>
                equiJoin (a, b, xs ::: List(createEqualityFunctions (function)) )(domainOf(a), domainOf(b))

            case _ => super.selection (relation, function)
        }).asInstanceOf[Rep[Query[Domain]]]
    }



    def createEqualityFunctions[A, B] (function: Exp[A => Boolean]): (Exp[Any => Boolean], Exp[Any => Boolean]) = {
        val params = parameters (function)
        if (params.size != 2) {
            throw new IllegalArgumentException ("Expected two parameters for function " + function)
        }
        body (function) match {
            case Def (Equal (lhs: Exp[Boolean@unchecked], rhs: Exp[Boolean@unchecked])) => {
                val usedByLeft = findSyms (lhs)(params.toSet)
                val usedByRight = findSyms (rhs)(params.toSet)
                if (usedByLeft.size != 1 || usedByRight.size != 1 && usedByLeft == usedByRight) {
                    throw new IllegalArgumentException (
                        "Expected equality that separates left and right parameter in function " + function)
                }
                val x = params (0)
                val y = params (1)
                if (usedByLeft == Set (x)) {
                    (dynamicLambda (x, lhs), dynamicLambda (y, rhs))
                }
                else
                {
                    (dynamicLambda (x, rhs), dynamicLambda (y, lhs))
                }
            }
            case _ => throw new IllegalArgumentException ("Expected equality in function " + function)
        }
    }
}
