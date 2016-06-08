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


import idb.algebra.ir.{RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRBasicOperators}
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.operations.{OptionOpsExp, SeqOpsExpExt, StringOpsExpExt}
import idb.lms.extensions.print.{CodeGenIndent, QuoteFunction}

import scala.virtualization.lms.common.{ScalaOpsPkgExp, StaticDataExp, StructExp, TupledFunctionsExp}

/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraPrintPlanRemoteOperators
    extends RelationalAlgebraPrintPlanBase
    with QuoteFunction
    with CodeGenIndent
{

    override val IR: ScalaOpsPkgExp with StructExp with StaticDataExp with OptionOpsExp with StringOpsExpExt with SeqOpsExpExt with TupledFunctionsExp with
        FunctionUtils with RelationalAlgebraIRRemoteOperators


    import IR.{Remote, Def, Exp, Reclassification}


    override def quoteRelation (x: Exp[Any]): String =
        x match {
            case Def (rel@Remote (relation, newHost)) =>
                withIndent (s"remote-->${rel.host}(\n") +
                    withMoreIndent (quoteRelation (relation) + "\n") +
                    withIndent (")")

            case Def (rel@Reclassification(r, newColor)) =>
				withIndent (s"reclass[${rel.host}](\n") +
					withMoreIndent (quoteRelation (r) + "\n") +
					withMoreIndent (newColor + "\n") +
					withIndent (")")


            case _ => super.quoteRelation (x)
        }

}
