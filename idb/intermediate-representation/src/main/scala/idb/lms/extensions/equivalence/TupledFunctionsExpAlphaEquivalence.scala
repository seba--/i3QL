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
package idb.lms.extensions.equivalence

import scala.virtualization.lms.common.TupledFunctionsExp
import idb.lms.extensions.FunctionUtils

/**
 *
 * @author Ralf Mitschke
 *
 */
trait TupledFunctionsExpAlphaEquivalence
    extends TupledFunctionsExp
    with FunctionsExpAlphaEquivalence
    with FunctionUtils
{
    override def isEquivalentDef[A, B] (a: Def[A], b: Def[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (fa@Lambda (_, UnboxedTuple (varsA), ba), fb@Lambda (_, UnboxedTuple (varsB), bb)) =>
                returnType(fa) == returnType(fb) && (varsA.map(_.tp) == varsB.map(_.tp)) && (
                    (varsA == varsB && isEquivalent (ba.res, bb.res)) ||
                        varsA.size == varsB.size &&
                            isEquivalent (ba.res, bb.res)(
                                varsA.zip (varsB).foldLeft (
                                    renamings
                                )(
                                    (renamingsAcc: VariableRenamings, pair: (Exp[_], Exp[_])) =>
                                        renamingsAcc.add (pair._1.asInstanceOf[Sym[_]], pair._2.asInstanceOf[Sym[_]]))
                            )
                    )

            case (Lambda (_, _: UnboxedTuple[_], _), Lambda (_, _: Sym[_], _)) => false

            case (Lambda (_, _: Sym[_], _), Lambda (_, _: UnboxedTuple[_], _)) => false

            case _ => super.isEquivalentDef (a, b)
        }

    override def isEquivalent[A, B] (a: Exp[A], b: Exp[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (UnboxedTuple(varsA), UnboxedTuple(varsB)) =>
                isEquivalentSeq(varsA, varsB)

            case _ => super.isEquivalent(a, b)
        }
}
