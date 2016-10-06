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
package idb.lms.extensions.reduction

import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.operations.TupleOpsExpExt
import scala.reflect.SourceContext
import scala.virtualization.lms.common.TupledFunctionsExp

/**
 *
 * @author Ralf Mitschke
 *
 */

trait TupledFunctionsExpBetaReduction
    extends TupledFunctionsExp
    with FunctionsExpBetaReduction
    with FunctionUtils
    with TupleOpsExpExt
{

    override protected def substitutions[A: Manifest] (oldX: Exp[A], x: Exp[A]): Map[Exp[Any], Exp[Any]] = {
        var result: Map[Exp[Any], Exp[Any]] = Map (oldX -> x)
        // we can replace any variable in an unboxed tuple, e.g., x in Unboxed(List(x,y)) by
        // a tuple access to the new boxed variable, e.g., x -> tuple2_get1(UnboxedTuple(List(newX,newY)))
        val oldParameters = parametersAsList (oldX)
		val appliedParameters = parametersAsList (unbox (x))
        if (oldParameters.size == appliedParameters.size) {
            result ++=
                oldParameters.zip (appliedParameters).foldLeft (
                    Map.empty[Exp[Any], Exp[Any]]
                )((map: Map[Exp[Any], Exp[Any]], params: (Exp[Any], Exp[Any])) => map + (params._1 -> params._2))
        }
        result
    }

    override def mirror[A: Manifest] (e: Def[A], f: Transformer)(implicit pos: SourceContext) =
        e match {
            case Apply (s, UnboxedTuple (xs)) => Apply (f (s), UnboxedTuple (f (xs)))
            case _ => super.mirror (e, f)
        }

    override def mirrorDef[A: Manifest] (e: Def[A], f: Transformer)(implicit pos: SourceContext) =
        e match {
            case Apply (s, UnboxedTuple (xs)) => Apply (f (s), UnboxedTuple (f (xs)))
            case _ => super.mirrorDef (e, f)
        }


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


}