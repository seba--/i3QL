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

import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.lms.extensions.functions.{TupledFunctionsExpDynamicLambda, FunctionsExpDynamicLambdaAlphaEquivalence}
import idb.lms.extensions.{FunctionUtils, ExpressionUtils}
import idb.query.QueryEnvironment
import scala.virtualization.lms.common._

/**
 * To simplify common sub-expression elimination and finding equivalent conditions, all selections are ordered via
 * their hash code; where greater hash codes come first
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptOrderSelections
    extends RelationalAlgebraIRBasicOperators
    with BaseFatExp
    with LiftBoolean
    with BooleanOpsExp
    with TupleOpsExp
    with TupledFunctionsExpDynamicLambda
    with EqualExp
    with ExpressionUtils
    with FunctionUtils
    with FunctionsExpDynamicLambdaAlphaEquivalence
{

    /**
     * Ordering selections via the hash code of their selection functions
     */
    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        relation match {
            case Def (Selection (r, f@Sym (id))) if id > function.asInstanceOf[Sym[_]].id =>
                selection (selection (r, function)(exactDomainOf (relation), queryEnvironment), f)

            case _ =>
                super.selection (relation, function)

        }


}
