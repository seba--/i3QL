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

import scala.virtualization.lms.common.{PrimitiveOpsExp, EqualExpOpt, OrderingOpsExp, NumericOpsExpOpt}
import scala.reflect.SourceContext

/**
 *
 * @author Ralf Mitschke
 */
trait ScalaOpsExpConstantPropagation
    extends NumericOpsExpOpt with OrderingOpsExp with EqualExpOpt with PrimitiveOpsExp
{

    //  override def equals[A: Manifest, B: Manifest] (a: Rep[A], b: Rep[B])
    //      (implicit pos: SourceContext): Rep[Boolean] =
    //    if (a == b)
    //      Const (true)
    //    else
    //    {
    //      (a, b) match
    //      {
    //        case (Const (true), b: DefMN[B, Boolean]) => b
    //        case (a: DefMN[A, Boolean], Const (true)) => a
    //        case _ => super.equals (a, b)
    //      }
    //    }


    override def numeric_plus[T: Numeric : Manifest] (lhs: Exp[T], rhs: Exp[T])
            (implicit pos: SourceContext): Exp[T] =
        (lhs, rhs) match {
            // e.g.,  (a + 1) + 2 := a + 3
            case (Def (NumericPlus (x, Const (v1))), Const (v2)) =>
                numeric_plus (x, Const (implicitly[Numeric[T]].plus (v1, v2)))
            // e.g.,  (1 + a) + 2 := a + 3
            case (Def (NumericPlus (Const (v1), x)), Const (v2)) =>
                numeric_plus (x, Const (implicitly[Numeric[T]].plus (v1, v2)))
            // super optimizations
            case _ => super.numeric_plus (lhs, rhs)
        }

    override def ordering_gt[T] (lhs: Exp[T], rhs: Exp[T])(
        implicit evidenceOrd: scala.math.Ordering[T],
        evidenceMan: scala.reflect.Manifest[T],
        pos: scala.reflect.SourceContext
    ): Rep[Boolean] =
        (lhs, rhs) match {
            // e.g., x + 2 > 0 := x > -2
            case (Def (NumericPlus (x, Const (v1))), Const (v2)) =>
                ordering_gt (x, Const (numericEvidenceFromOrdering.minus (v2, v1)))
            case _ => super.ordering_gt (lhs, rhs)
        }

    // TODO make an option
    def numericEvidenceFromOrdering[T] (implicit ord: Ordering[T]): Numeric[T] = {
        ord match {
            case Ordering.Int => Numeric.IntIsIntegral
            case _ => null
        }
    }
}
