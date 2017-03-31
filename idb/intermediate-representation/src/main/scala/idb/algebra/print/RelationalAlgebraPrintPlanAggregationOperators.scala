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
package idb.algebra.print

import idb.algebra.ir.RelationalAlgebraIRAggregationOperators
import idb.lms.extensions.{FunctionUtils, ScalaOpsPkgExpExtensions}
import idb.lms.extensions.operations.{OptionOpsExp, SeqOpsExpExt, StringOpsExpExt}
import idb.lms.extensions.print.{CodeGenIndent, QuoteFunction}

import scala.virtualization.lms.common.{ScalaOpsPkgExp, StaticDataExp, StructExp, TupledFunctionsExp}

/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 */
trait RelationalAlgebraPrintPlanAggregationOperators
    extends RelationalAlgebraPrintPlanBase
    with QuoteFunction
    with CodeGenIndent
{

    override val IR: ScalaOpsPkgExpExtensions with
		FunctionUtils with RelationalAlgebraIRAggregationOperators


	import IR.AggregationSelfMaintained
	import IR.AggregationNotSelfMaintained
    import IR.Def
    import IR.Exp


    override def quoteRelation (x: Exp[Any]): String =
        x match {

			case Def (r@AggregationSelfMaintained (relation, grouping, start, added, removed, updated, convertKey, convert)) =>
				withIndent (s"aggregationSelfMaintained[${r.host.name}](" + "\n") +
					withMoreIndent (quoteRelation (relation) + ",\n") +
					withMoreIndent (quoteFunction (grouping) + ",\n") +
					withMoreIndent (start + ",\n") +
					withMoreIndent (quoteFunction (added) + ",\n") +
					withMoreIndent (quoteFunction (removed) + ",\n") +
					withMoreIndent (quoteFunction (updated) + ",\n") +
					withMoreIndent (quoteFunction (convertKey) + ",\n") +
					withMoreIndent (quoteFunction (convert) + "\n") +
					withIndent (")")


			case Def (r@AggregationNotSelfMaintained (relation, grouping, start, added, removed, updated, convertKey, convert)) =>
				withIndent (s"aggregationNotSelfMaintained[${r.host.name}](\n") +
					withMoreIndent (quoteRelation (relation) + ",\n") +
					withMoreIndent (quoteFunction (grouping) + ",\n") +
					withMoreIndent (start + ",\n") +
					withMoreIndent (quoteFunction (added) + ",\n") +
					withMoreIndent (quoteFunction (removed) + ",\n") +
					withMoreIndent (quoteFunction (updated) + ",\n") +
					withMoreIndent (quoteFunction (convertKey) + ",\n") +
					withMoreIndent (quoteFunction (convert) + "\n") +
					withIndent (")")

            case _ => super.quoteRelation (x)
        }

}
