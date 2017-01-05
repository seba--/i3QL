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

import scala.virtualization.lms.common.{PrimitiveOpsExp, NumericOpsExp}

/**
 *
 * @author Ralf Mitschke
 *
 */

trait NumericOpsExpAlphaEquivalence
    extends NumericOpsExp
	with PrimitiveOpsExp
    with BaseExpAlphaEquivalence {

    override def isEquivalentDef[A, B] (a: Def[A], b: Def[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (NumericPlus (x, y), NumericPlus (u, v)) =>
                isEquivalent (x, u) && isEquivalent (y, v)
            case (NumericMinus (x, y), NumericMinus (u, v)) =>
                isEquivalent (x, u) && isEquivalent (y, v)
            case (NumericTimes (x, y), NumericTimes (u, v)) =>
                isEquivalent (x, u) && isEquivalent (y, v)
            case (NumericDivide (x, y), NumericDivide (u, v)) =>
                isEquivalent (x, u) && isEquivalent (y, v)

            case (IntPlus (x, y), IntPlus (u, v)) =>
                isEquivalent(x, u) && isEquivalent(y, v)

            case (DoubleFloatValue (a1), DoubleFloatValue (a2)) =>
                isEquivalent(a1, a2)
            case (DoubleToInt (a1), DoubleToInt (a2)) =>
                isEquivalent(a1, a2)
            case (DoubleToFloat (a1), DoubleToFloat (a2)) =>
                isEquivalent(a1, a2)
            case (DoublePlus (a1, b1), DoublePlus (a2, b2)) =>
                isEquivalent(a1, a2) && isEquivalent(b1, b2)
            case (DoubleMinus (a1, b1), DoubleMinus (a2, b2)) =>
                isEquivalent(a1, a2) && isEquivalent(b1, b2)
            case (DoubleTimes (a1, b1), DoubleTimes (a2, b2)) =>
                isEquivalent(a1, a2) && isEquivalent(b1, b2)
            case (DoubleDivide (a1, b1), DoubleDivide (a2, b2)) =>
                isEquivalent(a1, a2) && isEquivalent(b1, b2)

            case _ => super.isEquivalentDef (a, b)
        }


}
