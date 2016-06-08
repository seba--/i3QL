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
package idb.lms.extensions.functions

import idb.lms.extensions.operations.TupleOpsExpExt

import scala.reflect.SourceContext
import scala.virtualization.lms.common.TupledFunctionsExp
import scala.language.implicitConversions

/**
 *
 * @author Ralf Mitschke
 *
 */

trait TupledFunctionsExpDynamicLambda
    extends FunctionsExpDynamicLambda
    with TupledFunctionsExp
    with TupleOpsExpExt
{
    override implicit def toLambdaOpsAny[B: Manifest] (fun: Rep[Any => B]): LambdaOps[Any, B] =
        toLambdaOps (fun)


    override implicit def toLambdaOps2[A1: Manifest, A2: Manifest, B: Manifest] (fun: Rep[((A1, A2)) => B]) =
        new LambdaOps2 (fun)

    override implicit def toLambdaOps3[A1: Manifest, A2: Manifest, A3: Manifest, B: Manifest] (fun: Rep[((A1, A2,
        A3)) => B]) =
        new LambdaOps3 (fun)

    override implicit def toLambdaOps4[A1: Manifest, A2: Manifest, A3: Manifest, A4: Manifest,
    B: Manifest] (fun: Rep[((A1, A2, A3, A4)) => B]) =
        new LambdaOps4 (fun)

    override implicit def toLambdaOps5[A1: Manifest, A2: Manifest, A3: Manifest, A4: Manifest, A5: Manifest,
    B: Manifest] (
        fun: Rep[((A1, A2, A3, A4, A5)) => B]
    ) =
        new LambdaOps5 (fun)




    def dynamicLambda[A1, A2, B] (
        x1: Exp[A1], x2: Exp[A2], body: Exp[B]
    )(
        implicit pos: SourceContext
    ): Exp[((A1, A2)) => B] = {
        implicit val ma1 = x1.tp
        implicit val ma2 = x2.tp
        implicit val mb = body.tp
        dynamicLambdaDef (UnboxedTuple[(A1, A2)](scala.List (x1, x2)), body, manifest[(A1, A2)], manifest[B])
    }


}

