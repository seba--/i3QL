/* LiceUnionBSD Style License):
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
package idb.algebra.normalization

import idb.algebra.base.RelationalAlgebraDerivedOperators
import idb.algebra.ir._
import idb.lms.extensions._
import idb.query.QueryEnvironment
import scala.virtualization.lms.common._
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRNormalizeSubQueries
    extends RelationalAlgebraIRBasicOperators
    with RelationalAlgebraIRSubQueries
    with RelationalAlgebraNormalize
    with RelationalAlgebraDerivedOperators
    with TupledFunctionsExp
    with BooleanOpsExp
    with FunctionUtils
{

    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    )(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] =
        if (normalize) {


            function match {
                case Def (Lambda (f, x: Rep[Domain], body: Block[Boolean])) =>
                    body.res match {
                        // de-correlation of EXISTS( SELECT .... )
                        case Def (exists: ExistsCondition[_, Domain@unchecked]) =>
                            naturalJoin (
                                relation,
                                duplicateElimination (
                                    exists.createSubQueryWithContext (relation, parameter (function))
                                )
                            )

                        case Def (Reify (Def (exists: ExistsCondition[_, Domain@unchecked]), _, _)) =>
                            naturalJoin (
                                relation,
                                duplicateElimination (
                                    exists.createSubQueryWithContext (relation, parameter (function))
                                )
                            )

                        // de-correlation of NOT EXISTS( SELECT .... )
                        case Def (BooleanNegate (Def (exists: ExistsCondition[_, Domain@unchecked]))) =>
                            difference (
                                relation,
                                naturalJoin (
                                    relation,
                                    duplicateElimination (
                                        exists.createSubQueryWithContext (relation, parameter (function))
                                    )
                                )
                            )

                        case Def (Reify (Def (BooleanNegate (Def (exists: ExistsCondition[_, Domain@unchecked]))), _,_)) =>
                            difference (
                                relation,
                                naturalJoin (
                                    relation,
                                    duplicateElimination (
                                        exists.createSubQueryWithContext (relation, parameter (function))
                                    )
                                )
                            )

                        case _ => super.selection (relation, function)
                    }
                case _ => throw new IllegalArgumentException (function.toString + " is not a Lambda function")
            }
        } else
        {
            super.selection (relation, function)
        }

}


