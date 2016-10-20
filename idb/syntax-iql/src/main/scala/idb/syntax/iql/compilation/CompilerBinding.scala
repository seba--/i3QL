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
package idb.syntax.iql.compilation

import akka.actor.{ActorPath, ActorSystem}
import idb.Relation
import idb.algebra.compiler._
import idb.lms.extensions.ScalaGenDateOps
import idb.lms.extensions.operations.{ScalaGenEitherOps, ScalaGenOptionOps, ScalaGenSeqOpsExt, ScalaGenStringOpsExt}

import scala.language.postfixOps
import scala.virtualization.lms.common._

/**
 *
 * @author Ralf Mitschke
 */
case object CompilerBinding
    extends RelationalAlgebraGenBasicOperatorsAsIncremental
    with RelationalAlgebraGenSetTheoryOperatorsAsIncremental
    with RelationalAlgebraGenAggregationOperatorsAsIncremental
    with RelationalAlgebraGenRecursiveOperatorsAsIncremental
	with RelationalAlgebraGenRemoteOperatorsAsIncremental
    with RelationalAlgebraGenCacheAll
    with ScalaGenStaticData
    with ScalaGenOptionOps
	with ScalaGenEitherOps
    with ScalaGenStringOpsExt
    with ScalaGenSeqOpsExt
    with ScalaCodeGenPkg
    with ScalaGenStruct
    with ScalaGenTupledFunctions
    with ScalaGenDateOps
    //with ScalaGenFlightOps
{
    override val IR = idb.syntax.iql.IR

    override def reset {
      resetQueryCache()
      IR.reset
      super.reset
    }

	override def remoteFromPath[Domain](path: ActorPath): Relation[Domain] =
		RemoteUtils.from[Domain](path)

	override def remoteDeploy[Domain](system: ActorSystem, rel: Relation[Domain], path: ActorPath): Relation[Domain] = {
		val ref = RemoteUtils.deploy(system, path)(rel)
		RemoteUtils.from[Domain](ref)
	}
}
