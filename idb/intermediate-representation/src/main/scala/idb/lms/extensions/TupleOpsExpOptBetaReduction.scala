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
import scala.virtualization.lms.common.{IfThenElseExp, TupleOpsExp}

/**
 *
 * @author Ralf Mitschke
 */
trait TupleOpsExpOptBetaReduction
    extends TupleOpsExp with IfThenElseExp
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

}
