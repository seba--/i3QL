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
package idb.algebra.opt

import scala.virtualization.lms.common._
import idb.algebra.ir.RelationalAlgebraIRBasicOperators

/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptSimplify
    extends RelationalAlgebraIRBasicOperators
    with LiftBoolean
    with BooleanOps
    with BooleanOpsExp
    with EffectExp
    with TupledFunctionsExp
{

    private def isIdentity[Domain, Range] (function: Rep[Domain => Range]) = {
        function match {
            case Def (Lambda (_, UnboxedTuple (List (a, b)), Block (body)))
                if body == make_tuple2 (a, b) => true
            case Def (Lambda (_, UnboxedTuple (List (a, b, c)), Block (body)))
                if body == make_tuple3 (a, b, c) => true
            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d)), Block (body)))
                if body == make_tuple4 (a, b, c, d) => true
            case Def (Lambda (_, UnboxedTuple (List (a, b, c, d, e)), Block (body)))
                if body == make_tuple5 (a, b, c, d, e) => true
            case Def (Lambda (_, x, Block (body)))
                if body == x => true
            case _ => false
        }
    }

    /**
     * Remove projection that use the identity function
     */
    override def projection[Domain: Manifest, Range: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Range]
    ): Rep[Query[Range]] =
        if (isIdentity (function)) {
            relation.asInstanceOf[Rep[Query[Range]]]
        } else
        {
            super.projection (relation, function)
        }
}
