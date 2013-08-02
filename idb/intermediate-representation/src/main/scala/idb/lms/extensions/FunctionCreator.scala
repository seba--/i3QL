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
package idb.lms.extensions

import scala.virtualization.lms.common._

/**
 * TODO function recreation still takes up a lot of syms, even though they should be local.
 * @author Ralf Mitschke
 */
trait FunctionCreator
    extends BaseFatExp
    with TupledFunctionsExp
    with TupleOpsExp
    with EqualExp
    with FunctionsExpOptAlphaEquivalence
    with ExpressionUtils
    with FunctionUtils
{

    val transformer = new ForwardTransformer
    {
        val IR: FunctionCreator.this.type = FunctionCreator.this
    }

    def recreateFunRepDynamic[A, B] (
        x: Rep[A], body: Rep[B]
    ): Rep[A => B] = {
        val function = recreateFun (x, body)(x.tp, body.tp)
        fun (function)(x.tp, body.tp)
    }

    def recreateFun[A: Manifest, B: Manifest] (
        x: Rep[A], body: Rep[B]
    ): Rep[A] => Rep[B] =
        (t: Rep[A]) => {
            transformer.subst =
                x match {
                    case UnboxedTuple (args) =>
                        args match {
                            case List (a, b) =>
                                Map (
                                    a -> tuple2_get1 (t.asInstanceOf[Rep[Tuple2[Any, Any]]]),
                                    b -> tuple2_get2 (t.asInstanceOf[Rep[Tuple2[Any, Any]]])
                                )
                            case List (a, b, c) =>
                                Map (
                                    a -> tuple3_get1 (t.asInstanceOf[Rep[Tuple3[Any, Any, Any]]]),
                                    b -> tuple3_get2 (t.asInstanceOf[Rep[Tuple3[Any, Any, Any]]]),
                                    c -> tuple3_get3 (t.asInstanceOf[Rep[Tuple3[Any, Any, Any]]])
                                )
                            case List (a, b, c, d) =>
                                Map (
                                    a -> tuple4_get1 (t.asInstanceOf[Rep[Tuple4[Any, Any, Any, Any]]]),
                                    b -> tuple4_get2 (t.asInstanceOf[Rep[Tuple4[Any, Any, Any, Any]]]),
                                    c -> tuple4_get3 (t.asInstanceOf[Rep[Tuple4[Any, Any, Any, Any]]]),
                                    d -> tuple4_get4 (t.asInstanceOf[Rep[Tuple4[Any, Any, Any, Any]]])
                                )
                            case List (a, b, c, d, e) =>
                                Map (
                                    a -> tuple5_get1 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]]),
                                    b -> tuple5_get2 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]]),
                                    c -> tuple5_get3 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]]),
                                    d -> tuple5_get4 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]]),
                                    e -> tuple5_get5 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]])
                                )
                            case _ => throw new UnsupportedOperationException (
                                "Function with more than 5 parameters require special support")
                        }
                    // TODO refactor this to avoid code clone. Have the cases only return the list and then construct
                    // the mapping after pattern matching
                    case Def (ETuple2 (a, b)) =>
                        Map (
                            a -> tuple2_get1 (t.asInstanceOf[Rep[Tuple2[Any, Any]]]),
                            b -> tuple2_get2 (t.asInstanceOf[Rep[Tuple2[Any, Any]]])
                        )
                    case Def (ETuple3 (a, b, c)) =>
                        Map (
                            a -> tuple3_get1 (t.asInstanceOf[Rep[Tuple3[Any, Any, Any]]]),
                            b -> tuple3_get2 (t.asInstanceOf[Rep[Tuple3[Any, Any, Any]]]),
                            c -> tuple3_get3 (t.asInstanceOf[Rep[Tuple3[Any, Any, Any]]])
                        )
                    case Def (ETuple4 (a, b, c, d)) =>
                        Map (
                            a -> tuple4_get1 (t.asInstanceOf[Rep[Tuple4[Any, Any, Any, Any]]]),
                            b -> tuple4_get2 (t.asInstanceOf[Rep[Tuple4[Any, Any, Any, Any]]]),
                            c -> tuple4_get3 (t.asInstanceOf[Rep[Tuple4[Any, Any, Any, Any]]]),
                            d -> tuple4_get4 (t.asInstanceOf[Rep[Tuple4[Any, Any, Any, Any]]])
                        )
                    case Def (ETuple5 (a, b, c, d, e)) =>
                        Map (
                            a -> tuple5_get1 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]]),
                            b -> tuple5_get2 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]]),
                            c -> tuple5_get3 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]]),
                            d -> tuple5_get4 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]]),
                            e -> tuple5_get5 (t.asInstanceOf[Rep[Tuple5[Any, Any, Any, Any, Any]]])
                        )

                    case Sym (_) =>
                        Map (x -> t)
                    case _ => throw new UnsupportedOperationException (
                        "Function with parameters " + x.tp + " require special support")
                }
            val res = transformer.transformBlock (reifyEffects (body)).res
            transformer.subst = Map ()
            res
        }


    def createEqualityFunctions[A, B] (function: Exp[A => Boolean]): (Exp[Any => Boolean], Exp[Any => Boolean]) = {
        val params = parameters (function)
        if (params.size != 2) {
            throw new IllegalArgumentException ("Expected two parameters for function " + function)
        }
        body (function) match {
            case Def (Equal (lhs, rhs)) => {
                val usedByLeft = findSyms (lhs)(params.toSet)
                val usedByRight = findSyms (rhs)(params.toSet)
                if (usedByLeft.size != 1 || usedByRight.size != 1 && usedByLeft == usedByRight) {
                    throw new IllegalArgumentException (
                        "Expected equality that separates left and right parameter in function " + function)
                }
                val x = params (0)
                val y = params (1)
                if (usedByLeft == Set (x)) {
                    (recreateFunRepDynamic (x, lhs), recreateFunRepDynamic (y, rhs))
                }
                else
                {
                    (recreateFunRepDynamic (x, rhs), recreateFunRepDynamic (y, lhs))
                }
            }
                throw new IllegalArgumentException ("Expected equality in function " + function)
        }
    }
}
