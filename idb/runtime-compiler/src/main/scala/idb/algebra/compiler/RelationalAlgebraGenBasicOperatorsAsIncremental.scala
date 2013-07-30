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

import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.lms.extensions.CompileScalaExt
import idb.operators.impl._
import idb.operators.impl.opt._
import scala.virtualization.lms.common.ScalaGenEffect
import scala.virtualization.lms.common.FunctionsExp
import scala.virtualization.lms.common.FunctionsExp
import idb.MaterializedView

/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraGenBasicOperatorsAsIncremental
    extends RelationalAlgebraGenBaseAsIncremental
    with CompileScalaExt
    with ScalaGenEffect
{

    val IR: RelationalAlgebraIRBasicOperators with RelationalAlgebraGenSAEBinding with FunctionsExp

    import IR._

    // TODO incorporate set semantics into ir
    override def compile[Domain: Manifest] (query: Rep[Query[Domain]]): Relation[Domain] = {
        query match {
            case Def (Selection (r, f)) => {
                new SelectionView (compile (r), compileFunctionWithDynamicManifests (f), false)
            }
            case Def (Projection (r, f)) => {
                new ProjectionView (compile (r), compileFunctionWithDynamicManifests (f), false)
            }
            case Def (e@CrossProduct (a, b)) => {

				var compA = compile (a)
				var compB = compile (b)

				//TODO Remove this. Better: implement 'foreach' in Extent
				if (compA.isInstanceOf[Extent[_]])
					compA = compA.asMaterialized

				if (compB.isInstanceOf[Extent[_]])
					compB = compB.asMaterialized


                CrossProductView (compA, compB, false).asInstanceOf[Relation[Domain]]
            }
            case Def (e@EquiJoin (a, b, eq)) => {
				if (e.isIncrementLocal)
					TransactionalEquiJoinView (compile (a), compile (b), eq.map ((x) => compileFunctionWithDynamicManifests (x._1)),
						eq.map ((x) => compileFunctionWithDynamicManifests (x._2)), false).asInstanceOf[Relation[Domain]]
				else
                	EquiJoinView (compile (a), compile (b), eq.map ((x) => compileFunctionWithDynamicManifests (x._1)),
                    	eq.map ((x) => compileFunctionWithDynamicManifests (x._2)), false).asInstanceOf[Relation[Domain]]
            }
			case Def (e@UnionAdd (a, b)) => {
				new UnionViewAdd (compile (a) (e.mDomA), compile (b) (e.mDomB), false)
			}
			case Def (e@UnionMax (a, b)) => {
				if(e.isIncrementLocal)
					new TransactionalUnionMaxView(compile (a) (e.mDomA), compile (b) (e.mDomB), false)
				else
					new UnionViewMax (compile (a) (e.mDomA).asInstanceOf[MaterializedView[Domain]], compile (b) (e.mDomB).asInstanceOf[MaterializedView[Domain]], false)
			}
			case Def (e@Intersection (a, b)) => {
				if (e.isIncrementLocal)
					new TransactionalIntersectionView (compile (a), compile (b), false)
				else
					new IntersectionView (compile (a).asInstanceOf[MaterializedView[Domain]], compile (b).asInstanceOf[MaterializedView[Domain]], false)
			}
			case Def (e@Difference (a, b)) => {
				if (e.isIncrementLocal)
					new TransactionalDifferenceView (compile (a), compile (b), false)
				else
					new DifferenceView (compile (a), compile (b), false)
			}
			case Def (e@DuplicateElimination (a)) => {
				if(e.isIncrementLocal)
					new TransactionalDuplicateEliminationView (compile (a), false)
				else
					new DuplicateEliminationView(compile (a), false).asInstanceOf[Relation[Domain]]
			}
			case Def (e@TransitiveClosure (r, h, t)) => {
				if(e.isIncrementLocal)
					new TransactionalCyclicTransitiveClosureView(compile (r) (e.mEdge), compileFunctionWithDynamicManifests(h), compileFunctionWithDynamicManifests(t), false).asInstanceOf[Relation[Domain]]
				else
					new AcyclicTransitiveClosureView(compile (r) (e.mEdge), compileFunctionWithDynamicManifests(h), compileFunctionWithDynamicManifests(t), false).asInstanceOf[Relation[Domain]]
			}
			case Def (Unnest (r, f)) => {
				UnNestView(compile (r), compileFunctionWithDynamicManifests(f), false).asInstanceOf[Relation[Domain]]
			}
			case Def (Recursion (b, r)) => {
				RecursiveDRed(compile (b), compile (r), false)
			}
			case Def (e@AggregationSelfMaintained (r, fGroup, fAdd, fRemove, fUpdate, fConvert)) => {
				if (e.isIncrementLocal)
					TransactionalAggregation(compile(r),
						compileFunctionWithDynamicManifests(fGroup),
						compileFunctionWithDynamicManifests(fAdd),
						compileFunctionWithDynamicManifests(fRemove),
						compileFunctionWithDynamicManifests(fUpdate),
						compileFunctionWithDynamicManifests(fConvert),
						false)
				else
					AggregationForSelfMaintainableFunctions(compile(r),
					compileFunctionWithDynamicManifests(fGroup),
					compileFunctionWithDynamicManifests(fAdd),
					compileFunctionWithDynamicManifests(fRemove),
					compileFunctionWithDynamicManifests(fUpdate),
					compileFunctionWithDynamicManifests(fConvert),
					false)
			}
			case Def (Materialize (r)) => {
				compile(r).asMaterialized
			}
            case _ => super.compile (query)
        }
    }

}
