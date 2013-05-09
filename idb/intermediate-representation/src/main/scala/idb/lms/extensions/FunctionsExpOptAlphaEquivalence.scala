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

import scala.virtualization.lms.common.FunctionsExp

/**
 * Perform alpha reduction on lambda abstractions.
 *
 * @author Ralf Mitschke
 * @author Sebastian Erdweg
 */
trait FunctionsExpOptAlphaEquivalence
    extends FunctionsExp
{

    class LambdaAlpha[A: Manifest, B: Manifest] (f: Exp[A] => Exp[B], x: Exp[A], y: Block[B])
        extends Lambda[A, B](f, x, y)
    {

        override def equals (o: Any): Boolean = {
            if (!o.isInstanceOf[Lambda[A, B]])
                return false

            o.asInstanceOf[Lambda[A, B]] match {
                case Lambda (f2, x2, y2) =>
                    reifyEffects (f2 (
                        x)) == y // reify other lambda to the variable bound in this lambda,
                // and compare the resulting body with this body
                case _ =>
                    false
            }
        }

        // TODO define proper hashCode independent of bound variable name
        //   override def hashCode: Int = {
        //     return 0
        //   }
    }

    object LambdaAlpha
    {
        def apply[A: Manifest, B: Manifest] (f: Exp[A] => Exp[B], x: Exp[A], y: Block[B]) = new LambdaAlpha (f, x, y)
    }

    /**
     *
     * @return a lambda representation using alpha equivalence as equals test
     */
    override def doLambdaDef[A: Manifest, B: Manifest] (f: Exp[A] => Exp[B]): Def[A => B] = {
        val x = unboxedFresh[A]
        val y = reifyEffects (f (x)) // unfold completely at the definition site.

        LambdaAlpha (f, x, y)
    }


}
