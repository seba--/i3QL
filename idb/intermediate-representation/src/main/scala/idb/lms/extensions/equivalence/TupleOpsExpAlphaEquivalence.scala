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

import scala.virtualization.lms.common.TupleOpsExp

/**
 *
 * @author Ralf Mitschke
 *
 */

trait TupleOpsExpAlphaEquivalence
    extends TupleOpsExp
    with BaseExpAlphaEquivalence
{

    override def isEquivalent[A, B] (a: Exp[A], b: Exp[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (Def (ETuple2 (a_1, a_2)), Def (ETuple2 (b_1, b_2))) =>
                isEquivalent (a_1, b_1) &&
                    isEquivalent (a_2, b_2)

            case (Def (ETuple3 (a_1, a_2, a_3)), Def (ETuple3 (b_1, b_2, b_3))) =>
                isEquivalent (a_1, b_1) &&
                    isEquivalent (a_2, b_2) &&
                    isEquivalent (a_3, b_3)

            case (Def (ETuple4 (a_1, a_2, a_3, a_4)), Def (ETuple4 (b_1, b_2, b_3, b_4))) =>
                isEquivalent (a_1, b_1) &&
                    isEquivalent (a_2, b_2) &&
                    isEquivalent (a_3, b_3) &&
                    isEquivalent (a_4, b_4)

            case (Def (ETuple5 (a_1, a_2, a_3, a_4, a_5)), Def (ETuple5 (b_1, b_2, b_3, b_4, b_5))) =>
                isEquivalent (a_1, b_1) &&
                    isEquivalent (a_2, b_2) &&
                    isEquivalent (a_3, b_3) &&
                    isEquivalent (a_4, b_4) &&
                    isEquivalent (a_5, b_5)

            case (Def (Tuple2Access1 (t1)), Def (Tuple2Access1 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple2Access2 (t1)), Def (Tuple2Access2 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple3Access1 (t1)), Def (Tuple3Access1 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple3Access2 (t1)), Def (Tuple3Access2 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple3Access3 (t1)), Def (Tuple3Access3 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple4Access1 (t1)), Def (Tuple4Access1 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple4Access2 (t1)), Def (Tuple4Access2 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple4Access3 (t1)), Def (Tuple4Access3 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple4Access4 (t1)), Def (Tuple4Access4 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple5Access1 (t1)), Def (Tuple5Access1 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple5Access2 (t1)), Def (Tuple5Access2 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple5Access3 (t1)), Def (Tuple5Access3 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple5Access4 (t1)), Def (Tuple5Access4 (t2))) =>
                isEquivalent (t1, t2)

            case (Def (Tuple5Access5 (t1)), Def (Tuple5Access5 (t2))) =>
                isEquivalent (t1, t2)

            case _ => super.isEquivalent (a, b)
        }


}
