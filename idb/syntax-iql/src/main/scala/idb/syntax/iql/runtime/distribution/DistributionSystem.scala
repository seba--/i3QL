package idb.syntax.iql.runtime.distribution

import akka.actor.{ActorPath, ActorRef, ActorSystem, Deploy, Props}
import akka.remote.RemoteScope
import idb.query.RemoteHost
import idb.remote.Initialize
import idb.remote.receive.RefRemoteReceiver
import idb.syntax.iql

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
object DistributionSystem {

	val IR = iql.IR

	import IR._
	def distributeQuery[Domain : Manifest](system : ActorSystem, query : Rep[Query[Domain]]) : Relation[Domain] = {
		val node = query.node
		//TODO: Check if node is given
		val path = node.get.asInstanceOf[RemoteHost].path

		query match {
			case QueryTable (tbl, _, _, _) =>
				val ref = distribute(system, tbl, path)
				RefRemoteReceiver[Domain](ref)
		}
	}

	private def distribute[Domain](system : ActorSystem, relation : Relation[Domain], path : ActorPath) : ActorRef = {
		val ref = system.actorOf(
			Props(classOf[RemoteOperator[Domain]], relation)
				.withDeploy(Deploy(scope=RemoteScope(path.address)))
		)

		ref ! Initialize
		ref
	}

}
