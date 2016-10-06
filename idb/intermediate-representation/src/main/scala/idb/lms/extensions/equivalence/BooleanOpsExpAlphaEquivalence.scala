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

import scala.virtualization.lms.common.BooleanOpsExp

/**
 *
 * @author Ralf Mitschke
 *
 */

trait BooleanOpsExpAlphaEquivalence
    extends BooleanOpsExp
    with BaseExpAlphaEquivalence
{

    override def isEquivalentDef[A, B] (a: Def[A], b: Def[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (BooleanNegate (x), BooleanNegate (y)) =>
                isEquivalent (x, y)

            case (BooleanAnd (x, y), BooleanAnd (u, v)) =>
                symmetricEquivalent (x, y, u, v) || associativeEquivalentAnd (x, y, u, v)

            case (BooleanOr (x, y), BooleanOr (u, v)) =>
                symmetricEquivalent (x, y, u, v) || associativeEquivalentOr (x, y, u, v)

            case _ => super.isEquivalentDef (a, b)
        }

    private def symmetricEquivalent (aLeft: Exp[Any], aRight: Exp[Any], bLeft: Exp[Any], bRight: Exp[Any])(implicit renamings: VariableRenamings): Boolean =
        (isEquivalent (aLeft, bLeft) && isEquivalent (aRight, bRight)) || (
            isEquivalent (aLeft, bRight) && isEquivalent (aRight, bLeft))

    private def associativeEquivalentAnd (
        aLeft: Exp[Any],
        aRight: Exp[Any],
        bLeft: Exp[Any],
        bRight: Exp[Any]
    )(implicit renamings: VariableRenamings): Boolean = {
        (aLeft, aRight, bLeft, bRight) match {
            case (Def (BooleanAnd (x, y)), r1, Def (BooleanAnd (u, v)), r2) if isEquivalent (r1, r2) =>
                symmetricEquivalent (x, y, u, v)

            case (Def (BooleanAnd (x, y)), r1, l2, Def (BooleanAnd (u, v))) if isEquivalent (r1, l2) =>
                symmetricEquivalent (x, y, u, v)

            case (l1, Def (BooleanAnd (x, y)), Def (BooleanAnd (u, v)), r2) if isEquivalent (l1, r2) =>
                symmetricEquivalent (x, y, u, v)

            case (l1, Def (BooleanAnd (x, y)), l2, Def (BooleanAnd (u, v))) if isEquivalent (l1, l2) =>
                symmetricEquivalent (x, y, u, v)

            case _ => false
        }
    }

    private def associativeEquivalentOr (
        aLeft: Exp[Any],
        aRight: Exp[Any],
        bLeft: Exp[Any],
        bRight: Exp[Any]
    )(implicit renamings: VariableRenamings): Boolean = {
        (aLeft, aRight, bLeft, bRight) match {
            case (Def (BooleanOr (x, y)), r1, Def (BooleanOr (u, v)), r2) if isEquivalent (r1, r2) =>
                symmetricEquivalent (x, y, u, v)

            case (Def (BooleanOr (x, y)), r1, l2, Def (BooleanOr (u, v))) if isEquivalent (r1, l2) =>
                symmetricEquivalent (x, y, u, v)

            case (l1, Def (BooleanOr (x, y)), Def (BooleanOr (u, v)), r2) if isEquivalent (l1, r2) =>
                symmetricEquivalent (x, y, u, v)

            case (l1, Def (BooleanOr (x, y)), l2, Def (BooleanOr (u, v))) if isEquivalent (l1, l2) =>
                symmetricEquivalent (x, y, u, v)

            case _ => false
        }
    }

}
