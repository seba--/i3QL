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
package idb.algebra.ir

import akka.actor.ActorPath
import idb.algebra.base.RelationalAlgebraRemoteOperators
import idb.algebra.exceptions.NoServerAvailableException
import idb.query.{Host, QueryEnvironment}
import idb.query.taint.{Taint, TaintId}


/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIRRemoteOperators
    extends RelationalAlgebraIRBase with RelationalAlgebraRemoteOperators
{
    case class Remote[Domain : Manifest] (
        relation: Rep[Query[Domain]],
		host : Host
    ) extends Def[Query[Domain]] with QueryBaseOps {
		override def isSet = relation.isSet
    }

	case class Reclassification[Domain : Manifest] (
		relation : Rep[Query[Domain]],
		newTaint : Taint
	) extends Def[Query[Domain]] with QueryBaseOps {
		override def isSet = relation.isSet
		override def host = relation.host
	}

	case class Declassification[Domain : Manifest] (
		relation : Rep[Query[Domain]],
		taints : Set[TaintId]
	) extends Def[Query[Domain]] with QueryBaseOps {
		override def isSet = relation.isSet
		override def host = relation.host
	}

	case class ActorDef[Domain : Manifest] (
		actorPath : ActorPath,
		host : Host,
		taint : Taint
	) extends Def[Query[Domain]] with QueryBaseOps {
		override def isSet = false
	}


	override def remote[Domain: Manifest] (
		relation: Rep[Query[Domain]],
		host : Host
	)(implicit env : QueryEnvironment): Rep[Query[Domain]] =
		Remote(relation, host)


	override def reclassification[Domain : Manifest] (
		relation : Rep[Query[Domain]],
		newTaint : Taint
	)(implicit env : QueryEnvironment): Rep[Query[Domain]] =
		Reclassification(relation, newTaint)

	override def declassification[Domain : Manifest] (
		relation : Rep[Query[Domain]],
		taints : Set[TaintId]
	)(implicit env : QueryEnvironment): Rep[Query[Domain]] =
		Declassification(relation, taints)

	override def actorDef[Domain : Manifest](
		actorPath : ActorPath,
		host : Host,
		taint : Taint
	)(implicit env : QueryEnvironment): Rep[Query[Domain]] = {
		val hostPermissions = env.permissionsOf(host)
		if (!taint.ids.subsetOf(hostPermissions))
			throw new NoServerAvailableException(s"${host.name} has no permission for ${taint.ids}. Only has permissions: $hostPermissions")
		ActorDef[Domain](actorPath, host, taint)
	}




}



