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

import idb.algebra.exceptions.{NoServerAvailableException, UnknownHostDeployException}
import idb.algebra.ir._
import idb.lms.extensions.ScalaCodegenExt
import idb.operators.impl._
import idb.operators.impl.opt._
import idb.query._
import idb.remote.RemoteView

import scala.virtualization.lms.common._

/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraGenRemoteOperatorsAsIncremental
    extends RelationalAlgebraGenBaseAsIncremental
    with ScalaCodegenExt
    with ScalaGenEffect
{

    val IR: RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRSetTheoryOperators
		with RelationalAlgebraIRRecursiveOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraSAEBinding
		with FunctionsExp


    import IR._


    override def compile[Domain] (query: Rep[Query[Domain]])(implicit queryEnvironment : QueryEnvironment): Relation[Domain] = {
        query match {

            case Def (Remote (r, host)) =>
				//The hosts can be obtained like this:
				val childHost = r.host
				val parentHost = host

				parentHost match {
					case LocalHost =>
						//TODO: Send to local host
						compile(r)

					case NamedHost(_) =>
						//TODO: Send to local host
						compile(r)

					case UnknownHost =>
						throw new UnknownHostDeployException()

					case RemoteHost(name, address) =>
						//TODO: Send to remote host
						RemoteView (
							actorSystem = queryEnvironment.actorSystem,
							remoteSystem = address,
								partition = compile (r)
						)
				}



			case Def (Reclassification(r, _)) =>
				compile (r)

            case _ => super.compile (query)
        }
    }

}
