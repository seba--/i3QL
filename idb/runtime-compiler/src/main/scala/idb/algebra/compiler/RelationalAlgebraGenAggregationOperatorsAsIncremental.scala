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
 *  Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  Neither the name of the Software Technology Group or Technische
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
package idb.algebra.compiler

import idb.algebra.ir.{RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRAggregationOperators,
RelationalAlgebraIRRecursiveOperators, RelationalAlgebraIRBasicOperators}
import idb.lms.extensions.ScalaCodegenExt
import idb.operators.impl._
import idb.operators.impl.opt._
import idb.query.QueryEnvironment
import scala.virtualization.lms.common.ScalaGenEffect
import scala.virtualization.lms.common.FunctionsExp


/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraGenAggregationOperatorsAsIncremental
    extends RelationalAlgebraGenBaseAsIncremental
    with ScalaCodegenExt
    with ScalaGenEffect
{

    val IR: RelationalAlgebraIRBasicOperators
        with RelationalAlgebraIRSetTheoryOperators
        with RelationalAlgebraIRRecursiveOperators
        with RelationalAlgebraIRAggregationOperators
        with RelationalAlgebraSAEBinding
        with FunctionsExp

    import IR.Rep
    import IR.Def
    import IR.Query
    import IR.Relation
    import IR.AggregationSelfMaintained
    import IR.AggregationSelfMaintainedWithoutGrouping
	import IR.AggregationSelfMaintainedWithoutConvert
	import IR.AggregationNotSelfMaintained
	import IR.AggregationNotSelfMaintainedWithoutGrouping
	import IR.AggregationNotSelfMaintainedWithoutConvert
    import IR.Grouping

    override def compile[Domain] (query: Rep[Query[Domain]])(implicit queryEnvironment : QueryEnvironment): Relation[Domain] = {
        query match {
            case Def (e@AggregationSelfMaintained (r, fGroup, start, fAdd, fRemove, fUpdate, fConvertKey, fConvert)) => {
                if (e.isIncrementLocal)
					TransactionalAggregationSelfMaintained.applyTupled (
						compile (r),
						compileFunctionWithDynamicManifests (fGroup),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						compileFunctionWithDynamicManifests (fConvertKey),
						compileFunctionWithDynamicManifests (fConvert),
						false
					).asInstanceOf[Relation[Domain]]
                else
                    AggregationForSelfMaintainableFunctions.applyTupled (
                        compile (r),
                        compileFunctionWithDynamicManifests (fGroup),
						start,
                        compileFunctionWithDynamicManifests (fAdd),
                        compileFunctionWithDynamicManifests (fRemove),
                        compileFunctionWithDynamicManifests (fUpdate),
						compileFunctionWithDynamicManifests (fConvertKey),
						compileFunctionWithDynamicManifests (fConvert),
                        false
                    ).asInstanceOf[Relation[Domain]]
            }

            case Def (e@AggregationSelfMaintainedWithoutGrouping (r, start, fAdd, fRemove, fUpdate)) => {
                if (e.isIncrementLocal)
					TransactionalAggregationSelfMaintained (
						compile (r),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						false
					).asInstanceOf[Relation[Domain]]
                else
                    AggregationForSelfMaintainableFunctions (
                        compile (r),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
                        false
                    ).asInstanceOf[Relation[Domain]]
            }

			case Def (e@AggregationSelfMaintainedWithoutConvert (r, fGroup, start, fAdd, fRemove, fUpdate)) => {
				if (e.isIncrementLocal)
					TransactionalAggregationSelfMaintained (
						compile (r),
						compileFunctionWithDynamicManifests (fGroup),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						false
					).asInstanceOf[Relation[Domain]]
				else
					AggregationForSelfMaintainableFunctions (
						compile (r),
						compileFunctionWithDynamicManifests (fGroup),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						false
					).asInstanceOf[Relation[Domain]]

			}

			case Def (e@AggregationNotSelfMaintained (r, fGroup, start, fAdd, fRemove, fUpdate, fConvertKey, fConvert)) => {
				if (e.isIncrementLocal)
					TransactionalAggregationNotSelfMaintained.applyTupled (
						compile (r),
						compileFunctionWithDynamicManifests (fGroup),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						compileFunctionWithDynamicManifests (fConvertKey),
						compileFunctionWithDynamicManifests (fConvert),
						false
					).asInstanceOf[Relation[Domain]]
				else
					AggregationForNotSelfMaintainableFunctions.applyTupled (
						compile (r),
						compileFunctionWithDynamicManifests (fGroup),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						compileFunctionWithDynamicManifests (fConvertKey),
						compileFunctionWithDynamicManifests (fConvert),
						false
					).asInstanceOf[Relation[Domain]]
			}

			case Def (e@AggregationNotSelfMaintainedWithoutGrouping (r, start, fAdd, fRemove, fUpdate)) => {
				if (e.isIncrementLocal)
					TransactionalAggregationNotSelfMaintained (
						compile (r),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						false
					).asInstanceOf[Relation[Domain]]
				else
					AggregationForNotSelfMaintainableFunctions (
						compile (r),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						false
					).asInstanceOf[Relation[Domain]]
			}

			case Def (e@AggregationNotSelfMaintainedWithoutConvert (r, fGroup, start, fAdd, fRemove, fUpdate)) => {
				if (e.isIncrementLocal)
					TransactionalAggregationNotSelfMaintained (
						compile (r),
						compileFunctionWithDynamicManifests (fGroup),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						false
					).asInstanceOf[Relation[Domain]]
				else
					AggregationForNotSelfMaintainableFunctions (
						compile (r),
						compileFunctionWithDynamicManifests (fGroup),
						start,
						compileFunctionWithDynamicManifests (fAdd),
						compileFunctionWithDynamicManifests (fRemove),
						compileFunctionWithDynamicManifests (fUpdate),
						false
					).asInstanceOf[Relation[Domain]]

			}



            case Def (e@Grouping (r, fGroup)) => {
                if (e.isIncrementLocal)
					TransactionalAggregationSelfMaintained (
						compile (r),
						compileFunctionWithDynamicManifests (fGroup),
						false
					)
                else
                    AggregationForSelfMaintainableFunctions (
                        compile (r),
                        compileFunctionWithDynamicManifests (fGroup),
                        false
                    )
            }


            case _ => super.compile (query)
        }
    }

}
