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

import akka.actor.ActorSystem
import idb.algebra.{RelationalAlgebraIREssentialsPackage, RelationalAlgebraIROperatorsPackage}
import idb.algebra.ir.RelationalAlgebraIRBase
import idb.algebra.remote.PlacementStrategy
import idb.algebra.remote.placement.StandardPlacementTransformer
import idb.lms.extensions.RemoteUtils
import idb.query.QueryEnvironment



/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraGenBaseAsIncremental extends PlacementStrategy {

	val IR: RelationalAlgebraIREssentialsPackage
		with RelationalAlgebraSAEBinding

    import IR._

    def initialize(system : ActorSystem, relation : Relation[_]) : Unit

    def compile[Domain : Manifest] (query: Rep[Query[Domain]])(implicit env : QueryEnvironment): Relation[Domain] =
        query match {
            case QueryRelation (relation, _, _, _) => relation

            case QueryTable (tbl, _, _, _) => tbl

			case r@Def (Root (q, host)) =>
               val qt = q //transform(q)
                val rel = compile (qt)
	           // initialize(env.system, rel)
	            rel



            case Def(r) =>
                throw new UnsupportedOperationException ("Compilation not supported for queries of type: " + r.getClass)
        }

}
