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
package idb.lms.extensions.normalization

import scala.reflect.SourceContext
import scala.virtualization.lms.common.BooleanOpsExp

/**
 * Orders the terms in boolean expressions.
 * The expression trees will be right-deep trees, i.e.,
 *   &
 *  / \
 * a   &
 *    / \
 *   b   &
 *
 * The individual terms are sorted by sym ids, i.e., larger ids are further up in the tree.
 * Negations are sorted by the ids of the contained expression, so we can match "a && !a"
 *
 * @author Ralf Mitschke
 *
 */
trait BooleanOpsExpOrdering
    extends BooleanOpsExp
{

    override def boolean_negate (lhs: Exp[Boolean])(implicit pos: SourceContext): Exp[Boolean] =
        super.boolean_negate (lhs)


    override def boolean_and (lhs: Exp[Boolean], rhs: Exp[Boolean])(implicit pos: SourceContext): Exp[Boolean] =
        (lhs, rhs) match {
            case (Def (BooleanAnd (left, right)), _) =>
                boolean_and (left, boolean_and (right, rhs))

            case (Def (BooleanNegate (Sym (lhsNegId))), Def (BooleanNegate (Sym (rhsNegId)))) =>
                if (lhsNegId < rhsNegId)
                    boolean_and (rhs, lhs)
                else
                    super.boolean_and (lhs, rhs)

            case (
                Def (BooleanNegate (Sym (lhsNegId))),
                Def (BooleanAnd (left@Def (BooleanNegate (Sym (leftId))), right))
                ) =>
                if (lhsNegId < leftId)
                    boolean_and (left, boolean_and (lhs, right))
                else
                    super.boolean_and (lhs, rhs)

            case (Def (BooleanNegate (Sym (lhsNegId))), Def (BooleanAnd (left@Sym (leftId), right))) =>
                if (lhsNegId < leftId)
                    boolean_and (left, boolean_and (lhs, right))
                else
                    super.boolean_and (lhs, rhs)

            case (Def (BooleanNegate (Sym (lhsNegId))), Sym (rhsId)) =>
                if (lhsNegId < rhsId)
                    boolean_and (rhs, lhs)
                else
                    super.boolean_and (lhs, rhs)

            case (Sym (lhsId), Def (BooleanNegate (Sym (rhsNegId)))) =>
                if (lhsId < rhsNegId)
                    boolean_and (rhs, lhs)
                else
                    super.boolean_and (lhs, rhs)

            case (Sym (lhsId), Def (BooleanAnd (left@Def (BooleanNegate (Sym (leftId))), right))) =>
                if (lhsId < leftId)
                    boolean_and (left, boolean_and (lhs, right))
                else
                    super.boolean_and (lhs, rhs)

            case (Sym (lhsId), Def (BooleanAnd (left@Sym (leftId), right))) =>
                if (lhsId < leftId)
                    boolean_and (left, boolean_and (lhs, right))
                else
                    super.boolean_and (lhs, rhs)

            case _ => super.boolean_and (lhs, rhs)

        }


    override def boolean_or (lhs: Exp[Boolean], rhs: Exp[Boolean])(implicit pos: SourceContext): Exp[Boolean] =
        (lhs, rhs) match {
            case (Def (BooleanOr (left, right)), _) =>
                boolean_or (left, boolean_or (right, rhs))

            case (Def (BooleanNegate (Sym (lhsNegId))), Def (BooleanNegate (Sym (rhsNegId)))) =>
                if (lhsNegId < rhsNegId)
                    boolean_or (rhs, lhs)
                else
                    super.boolean_or (lhs, rhs)

            case (
                Def (BooleanNegate (Sym (lhsNegId))),
                Def (BooleanOr (left@Def (BooleanNegate (Sym (leftId))), right))
                ) =>
                if (lhsNegId < leftId)
                    boolean_or (left, boolean_or (lhs, right))
                else
                    super.boolean_or (lhs, rhs)

            case (Def (BooleanNegate (Sym (lhsNegId))), Def (BooleanOr (left@Sym (leftId), right))) =>
                if (lhsNegId < leftId)
                    boolean_or (left, boolean_or (lhs, right))
                else
                    super.boolean_or (lhs, rhs)

            case (Def (BooleanNegate (Sym (lhsNegId))), Sym (rhsId)) =>
                if (lhsNegId < rhsId)
                    boolean_or (rhs, lhs)
                else
                    super.boolean_or (lhs, rhs)

            case (Sym (lhsId), Def (BooleanNegate (Sym (rhsNegId)))) =>
                if (lhsId < rhsNegId)
                    boolean_or (rhs, lhs)
                else
                    super.boolean_or (lhs, rhs)

            case (Sym (lhsId), Def (BooleanOr (left@Def (BooleanNegate (Sym (leftId))), right))) =>
                if (lhsId < leftId)
                    boolean_or (left, boolean_or (lhs, right))
                else
                    super.boolean_or (lhs, rhs)

            case (Sym (lhsId), Def (BooleanOr (left@Sym (leftId), right))) =>
                if (lhsId < leftId)
                    boolean_or (left, boolean_or (lhs, right))
                else
                    super.boolean_or (lhs, rhs)


            case _ => super.boolean_or (lhs, rhs)
        }


}
