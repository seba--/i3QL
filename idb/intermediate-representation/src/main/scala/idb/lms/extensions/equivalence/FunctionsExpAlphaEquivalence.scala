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

import scala.virtualization.lms.common.FunctionsExp
import idb.lms.extensions.FunctionUtils


/**
 *
 * @author Ralf Mitschke
 *
 */

trait FunctionsExpAlphaEquivalence
    extends FunctionsExp
    with BaseExpAlphaEquivalence
    with FunctionUtils
{

    override def isEquivalentDef[A, B] (a: Def[A], b: Def[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (fa@Lambda (_, xa: Sym[_], ba), fb@Lambda (_, xb: Sym[_], bb)) =>
                // functions need to have the same return type
                // while there is such a thing as allowing covariance in inheritance,
                // we can not generate the same function with a covariant type in a totally different place
                returnType (fa) == returnType (fb) && xa.tp == xb.tp && (
                    // either same variable is used, or we add a renaming
                    (xa == xb && isEquivalent (ba.res, bb.res)) || isEquivalent (ba.res, bb.res)(renamings.add (xa, xb))
                    )

            case (Apply (fa, xa), Apply (fb, xb)) =>
                isEquivalent (xa, xb) && isEquivalent (fa, fb)

            case _ => super.isEquivalentDef (a, b)
        }


}
