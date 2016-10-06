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
package idb.lms.extensions.simplification

import scala.reflect.SourceContext
import scala.virtualization.lms.common.BooleanOpsExp

/**
 * Simplifies boolean expressions
 *
 * @author Ralf Mitschke
 *
 */
trait BooleanOpsExpSimplification
    extends BooleanOpsExp
{

    override def boolean_negate (lhs: Exp[Boolean])(implicit pos: SourceContext): Exp[Boolean] =
        lhs match {
            case Const (true) =>
                Const (false)

            case Const (false) =>
                Const (true)

            case _ =>
                super.boolean_negate (lhs)
        }


    override def boolean_and (lhs: Exp[Boolean], rhs: Exp[Boolean])(implicit pos: SourceContext): Exp[Boolean] =
        (lhs, rhs) match {
            case (Def (BooleanNegate (`rhs`)), _) =>
                Const (false)

            case (_, Def (BooleanNegate (`lhs`))) =>
                Const (false)

            case (_, Def(BooleanAnd (Def (BooleanNegate (`lhs`)), _))) =>
                Const (false)

            case (Const (true), _) =>
                rhs

            case (_, Const (true)) =>
                lhs

            case (Const (false), _) =>
                Const (false)

            case (_, Const (false)) =>
                Const (false)

            case (_, Def(BooleanAnd (`lhs`, r))) =>
                boolean_and(lhs, r)

            case _ if lhs == rhs => lhs


            case _ => super.boolean_and (lhs, rhs)
        }


    override def boolean_or (lhs: Exp[Boolean], rhs: Exp[Boolean])(implicit pos: SourceContext): Exp[Boolean] =
        (lhs, rhs) match {
            case (Def (BooleanNegate (`rhs`)), _) =>
                Const (true)

            case (_, Def (BooleanNegate (`lhs`))) =>
                Const (true)

            case (_, Def(BooleanOr (Def (BooleanNegate (`lhs`)), _))) =>
                Const (false)

            case (Const (true), _) =>
                Const (true)

            case (_, Const (true)) =>
                Const (true)

            case (Const (false), _) =>
                rhs

            case (_, Const (false)) =>
                lhs

            case (_, Def(BooleanOr (`lhs`, r))) =>
                boolean_or(lhs, r)

            case _ if lhs == rhs => lhs

            case _ => super.boolean_or (lhs, rhs)
        }


}
