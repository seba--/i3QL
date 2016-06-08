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

import scala.virtualization.lms.common.FunctionsExp
import scala.reflect.SourceContext
import idb.lms.extensions.FunctionUtils
import scala.language.implicitConversions

/**
 *
 * @author Ralf Mitschke
 *
 */

trait FunctionsExpDynamicLambda
    extends FunctionsExp
    with FunctionUtils
{

    override implicit def toLambdaOps[A:Manifest,B:Manifest](fun: Rep[A => B]) : LambdaOps[A, B]= {
        implicit val mA = parameterType (fun).asInstanceOf[Manifest[A]]
        implicit val mB = returnType (fun).asInstanceOf[Manifest[B]]
        new LambdaOps (fun)(mA, mB)
    }


    private val illegalFunctionCall: Exp[Any] => Exp[Any] =
        (x: Exp[Any]) => throw new
                IllegalStateException ("Tried to call original Scala method of a dynamic lambda function")


    class DynamicLambda[A: Manifest, B: Manifest] (x: Exp[A], y: Block[B])
        extends Lambda[A, B](illegalFunctionCall.asInstanceOf[Exp[A] => Exp[B]], x, y)
    {

    }

    def dynamicLambdaDef[A, B] (x: Exp[A], body: Exp[B], mA : Manifest[A], mB : Manifest[B]): Def[A => B] = {
        implicit val _mA = mA
        implicit val _mB = mB
        val block = reifyEffects (body)
        new DynamicLambda (x, block)
    }

    def dynamicLambda[A, B] (x: Exp[A], body: Exp[B])(implicit pos: SourceContext): Exp[A => B] = {
        implicit val ma = x.tp
        implicit val mb = body.tp
        dynamicLambdaDef (x, body, ma, mb)
    }


}

