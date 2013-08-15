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

import scala.reflect.SourceContext
import scala.virtualization.lms.common.{TupledFunctionsExp, IfThenElseExp, TupleOpsExp}

/**
 *
 * @author Ralf Mitschke
 */
trait TupleOpsExpOptBetaReduction
    extends TupleOpsExp
    with TupledFunctionsExp
    with IfThenElseExp
{

    override implicit def make_tuple2[A: Manifest, B: Manifest] (
        t: (Exp[A], Exp[B])
    )(implicit pos: SourceContext): Exp[(A, B)] =
        t match {
            case (Def (Tuple2Access1 (a)), Def (Tuple2Access2 (b)))
                if a == b => a.asInstanceOf[Exp[(A, B)]]
            case _ => super.make_tuple2 (t)
        }

    override implicit def make_tuple3[A: Manifest, B: Manifest, C: Manifest] (
        t: (Exp[A], Exp[B], Exp[C])
    )(implicit pos: SourceContext): Exp[(A, B, C)] =
        t match {
            case (Def (Tuple3Access1 (a)), Def (Tuple3Access2 (b)), Def (Tuple3Access3 (c)))
                if a == b && a == c => a.asInstanceOf[Exp[(A, B, C)]]
            case _ => super.make_tuple3 (t)
        }


    override implicit def make_tuple4[A: Manifest, B: Manifest, C: Manifest, D: Manifest] (
        t: (Exp[A], Exp[B], Exp[C], Exp[D])
    )(implicit pos: SourceContext): Exp[(A, B, C, D)] =
        t match {
            case (Def (Tuple4Access1 (a)), Def (Tuple4Access2 (b)), Def (Tuple4Access3 (c)), Def (Tuple4Access4 (d)))
                if a == b && a == c && a == d => a.asInstanceOf[Exp[(A, B, C, D)]]
            case _ => super.make_tuple4 (t)
        }

    override implicit def make_tuple5[A: Manifest, B: Manifest, C: Manifest, D: Manifest, E: Manifest] (
        t: (Exp[A], Exp[B], Exp[C], Exp[D], Exp[E])
    )(implicit pos: SourceContext
    ): Exp[(A, B, C, D, E)] =
        t match {
            case (Def (Tuple5Access1 (a)), Def (Tuple5Access2 (b)), Def (Tuple5Access3 (c)), Def (
            Tuple5Access4 (d)), Def (Tuple5Access5 (e)))
                if a == b && a == c && a == d && a == e => a.asInstanceOf[Exp[(A, B, C, D, E)]]
            case _ => super.make_tuple5 (t)
        }

    /*
    override def tuple2_get1[A] (t: Exp[Tuple2[A, _]])
            (implicit evidence$85: Manifest[A], pos: SourceContext): Rep[A] =
        t match {
            case UnboxedTuple (vars) => vars (0).asInstanceOf[Rep[A]]
            case _ => super.tuple2_get1 (t)
        }

    override def tuple2_get2[B] (t: Exp[Tuple2[_, B]])
            (implicit evidence$86: Manifest[B], pos: SourceContext): Rep[B] =
        t match {
            case UnboxedTuple (vars) => vars (1).asInstanceOf[Rep[B]]
            case _ => super.tuple2_get2 (t)
        }


    override def tuple3_get1[A] (t: Exp[Tuple3[A, _, _]])
            (implicit evidence$87: Manifest[A], pos: SourceContext): Rep[A] =
        t match {
            case UnboxedTuple (vars) => vars (0).asInstanceOf[Rep[A]]
            case _ => super.tuple3_get1 (t)
        }

    override def tuple3_get2[B] (t: Exp[Tuple3[_, B, _]])
            (implicit evidence$88: Manifest[B], pos: SourceContext): Rep[B] =
        t match {
            case UnboxedTuple (vars) => vars (1).asInstanceOf[Rep[B]]
            case _ => super.tuple3_get2 (t)
        }

    override def tuple3_get3[C] (t: Exp[Tuple3[_, _, C]])
            (implicit evidence$89: Manifest[C], pos: SourceContext): Rep[C] =
        t match {
            case UnboxedTuple (vars) => vars (2).asInstanceOf[Rep[C]]
            case _ => super.tuple3_get3 (t)
        }

    override def tuple4_get1[A] (t: Exp[Tuple4[A, _, _, _]])
            (implicit evidence$90: Manifest[A], pos: SourceContext): Rep[A] =
        t match {
            case UnboxedTuple (vars) => vars (0).asInstanceOf[Rep[A]]
            case _ => super.tuple4_get1 (t)
        }

    override def tuple4_get2[B] (t: Exp[Tuple4[_, B, _, _]])
            (implicit evidence$91: Manifest[B], pos: SourceContext): Rep[B] =
        t match {
            case UnboxedTuple (vars) => vars (1).asInstanceOf[Rep[B]]
            case _ => super.tuple4_get2 (t)
        }

    override def tuple4_get3[C] (t: Exp[Tuple4[_, _, C, _]])
            (implicit evidence$92: Manifest[C], pos: SourceContext): Rep[C] =
        t match {
            case UnboxedTuple (vars) => vars (2).asInstanceOf[Rep[C]]
            case _ => super.tuple4_get3 (t)
        }

    override def tuple4_get4[D] (t: Exp[Tuple4[_, _, _, D]])
            (implicit evidence$93: Manifest[D], pos: SourceContext): Rep[D] =
        t match {
            case UnboxedTuple (vars) => vars (3).asInstanceOf[Rep[D]]
            case _ => super.tuple4_get4 (t)
        }

    override def tuple5_get1[A] (t: Exp[Tuple5[A, _, _, _, _]])
            (implicit evidence$94: Manifest[A], pos: SourceContext): Rep[A] =
        t match {
            case UnboxedTuple (vars) => vars (0).asInstanceOf[Rep[A]]
            case _ => super.tuple5_get1 (t)
        }

    override def tuple5_get2[B] (t: Exp[Tuple5[_, B, _, _, _]])
            (implicit evidence$95: Manifest[B], pos: SourceContext): Rep[B] =
        t match {
            case UnboxedTuple (vars) => vars (1).asInstanceOf[Rep[B]]
            case _ => super.tuple5_get2 (t)
        }

    override def tuple5_get3[C] (t: Exp[Tuple5[_, _, C, _, _]])
            (implicit evidence$96: Manifest[C], pos: SourceContext): Rep[C] =
        t match {
            case UnboxedTuple (vars) => vars (2).asInstanceOf[Rep[C]]
            case _ => super.tuple5_get3 (t)
        }


    override def tuple5_get4[D] (t: Exp[Tuple5[_, _, _, D, _]])
            (implicit evidence$97: Manifest[D], pos: SourceContext): Rep[D] =
        t match {
            case UnboxedTuple (vars) => vars (3).asInstanceOf[Rep[D]]
            case _ => super.tuple5_get4 (t)
        }


    override def tuple5_get5[E] (t: Exp[Tuple5[_, _, _, _, E]])
            (implicit evidence$98: Manifest[E], pos: SourceContext): Rep[E] =
        t match {
            case UnboxedTuple (vars) => vars (4).asInstanceOf[Rep[E]]
            case _ => super.tuple5_get5 (t)
        }

    */
}
