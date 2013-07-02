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
trait FunctionUtils
    extends ForwardTransformer
{

    val IR: BaseFatExp with EffectExp with TupleOpsExp with TupledFunctionsExp with FunctionsExpOptAlphaEquivalence with ExpressionUtils

    import IR.Rep
    import IR.Def
    import IR.Sym
    import IR.reifyEffects
    import IR.ETuple2
    import IR.tuple2_get1
    import IR.tuple2_get2
    import IR.UnboxedTuple

    def recreateFun[A: Manifest, B: Manifest](
        x: Rep[A], body: Rep[B]
    ): Rep[A] => Rep[B] =
        (t: Rep[A]) => {
            subst =
                x match {
                    case Def (ETuple2 (a, b)) =>
                        Map (a -> tuple2_get1 (t.asInstanceOf[Rep[Tuple2[Any,Any]]]), b -> tuple2_get2 (t.asInstanceOf[Rep[Tuple2[Any,Any]]]))
                    case UnboxedTuple(List(a, b)) =>
                        Map(a -> tuple2_get1(t.asInstanceOf[Rep[Tuple2[Any,Any]]]), b -> tuple2_get2(t.asInstanceOf[Rep[Tuple2[Any,Any]]]) )
                    case Sym (_) =>
                        Map (x -> t)
                    case _ => throw new UnsupportedOperationException("Function with parameters " + x.tp + " require special support")
                }
            val res = transformBlock (reifyEffects (body)).res
            subst = Map()
            res
        }
}
