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

import scala.virtualization.lms.common.Base
import scala.language.implicitConversions
import scala.language.higherKinds

/**
 *
 * @author Ralf Mitschke
 *
 */

trait BaseAlphaEquivalence
    extends Base
{

    type VarExp[+T] // Var and Variable are already used somewhere in LMS

    def isEquivalent[A, B] (a: Rep[A], b: Rep[B])(implicit renamings: VariableRenamings = emptyRenaming): Boolean

    def isEquivalentSeq[A, B] (
        xsa: Seq[Rep[A]], xsb: Seq[Rep[B]]
    )(
        implicit renamings: VariableRenamings = emptyRenaming
    ): Boolean = {
        xsa.zip (xsb).foreach (p =>
            if (!isEquivalent (p._1, p._2))
                return false
        )
        true
    }

    /**
     * Captures a set of possible reanmings for variables
     * Variable renamings are always commutative, i.e., adding a renaming (a, b) is equivalent to adding (b, a) and
     * canRename will return true for (a,b) or (b,a)
     */
    trait VariableRenamings
    {

        def add[T] (x: VarExp[T], y: VarExp[T]): VariableRenamings

        def canRename[T] (x: VarExp[T], y: VarExp[T]): Boolean

    }

    def emptyRenaming: VariableRenamings
}
