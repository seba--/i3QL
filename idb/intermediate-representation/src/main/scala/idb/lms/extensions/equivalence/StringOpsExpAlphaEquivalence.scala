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

import idb.lms.extensions.operations.StringOpsExpExt

/**
 *
 * @author Ralf Mitschke
 *
 */

trait StringOpsExpAlphaEquivalence
    extends StringOpsExpExt
    with BaseExpAlphaEquivalence
{

    override def isEquivalentDef[A, B] (a: Def[A], b: Def[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (StringPlus (s1, o1), StringPlus (s2, o2)) =>
                isEquivalent (s1, s2) &&
                    isEquivalent (o1, o2)

            case (StringStartsWith (s1, starts1), StringStartsWith (s2, starts2)) =>
                isEquivalent (s1, s2) &&
                    isEquivalent (starts1, starts2)

            case (StringTrim (s1), StringTrim (s2)) =>
                isEquivalent (s1, s2)

            case (StringSplit (s1, separators1, limit1), StringSplit (s2, separators2, limit2)) =>
                isEquivalent (s1, s2) &&
                    isEquivalent (separators1, separators2) &&
					isEquivalent (limit1, limit2)


            case (StringValueOf (x1), StringValueOf (x2)) =>
                isEquivalent (x1, x2)

            case (StringToDouble (s1), StringToDouble (s2)) =>
                isEquivalent (s1, s2)

            case (StringToFloat (s1), StringToFloat (s2)) =>
                isEquivalent (s1, s2)

            case (StringToInt (s1), StringToInt (s2)) =>
                isEquivalent (s1, s2)

            case (StringLastIndexOf (s1, c1), StringLastIndexOf (s2, c2)) =>
                isEquivalent (s1, s2) && isEquivalent (c1, c2)

			case (StringEndsWith (s1, e1), StringEndsWith (s2, e2)) =>
				isEquivalent (s1, s2) && isEquivalent (e1, e2)

			case (StringIndexOf (s1, e1), StringIndexOf (s2, e2)) =>
				isEquivalent (s1, s2) && isEquivalent (e1, e2)

			case (StringToLowerCase (s1), StringToLowerCase (s2)) =>
				isEquivalent(s1, s2)

			case (StringSubstring (s1, i1, j1), StringSubstring (s2, i2, j2)) =>
				isEquivalent(s1, s2) && isEquivalent(i1, i2) && isEquivalent(j1, j2)

			case (StringLength (s1), StringLength(s2)) =>
				isEquivalent(s1, s2)

            case _ => super.isEquivalentDef (a, b)
        }

}
