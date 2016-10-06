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

import scala.virtualization.lms.common.StructExp

/**
 *
 * @author Ralf Mitschke
 *
 */

trait StructExpAlphaEquivalence
    extends StructExp
    with BaseExpAlphaEquivalence
{

    override def isEquivalent[A, B] (a: Exp[A], b: Exp[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {

           /* case (Def (Field (s1, i1, t1)), Def (Field (s2, i2, t2))) =>
                i1 == i2 && t1 == t2 && isEquivalent (s1, s2)  */

			case (Def (Field (struct1, name1)), Def (Field (struct2, name2))) =>
				name1 == name2 && isEquivalent (struct1, struct2)

            case (Def (Struct (tag1, map1)), Def (Struct (tag2, map2))) => {
                if (tag1 != tag2)
                {
                    return false
                }
                map1.zip (map2).foreach (
                    pair => {
                        if (pair._1._1 != pair._2._1 ||
                            !isEquivalent (pair._1._2, pair._2._2)
                        ) return false
                    }
                )
                true
            }

            case _ => super.isEquivalent (a, b)
        }

}
