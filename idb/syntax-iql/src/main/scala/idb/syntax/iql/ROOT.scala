package idb.syntax.iql

import akka.actor.ActorPath
import idb.algebra.remote.PlacementStrategy
import idb.query.{Host, QueryEnvironment, RemoteHost}
import idb.query.taint.Taint
import idb.syntax.iql.IR._
import idb.syntax.iql.compilation.RemoteUtils

/**
 * @author Mirko Köhler
 */
object ROOT {

	def UNSAFE[Domain : Manifest](host : ActorPath, query : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Relation[Domain] = {
		val relation : Relation[Domain] = query
		UNSAFE(host, relation)
	}

	def UNSAFE[Domain : Manifest](host : ActorPath, relation : Relation[Domain])(implicit env : QueryEnvironment) : Relation[Domain] = {
		val ref = RemoteUtils.deploy(env.system, host)(relation)
		val r = RemoteUtils.fromWithDeploy(env.system, ref)
		r
	}

	def UNSAFE[Domain : Manifest](host : RemoteHost, query : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Relation[Domain] =
		UNSAFE(host.path, query)


	def UNSAFE[Domain : Manifest](host : RemoteHost, relation : Relation[Domain])(implicit env : QueryEnvironment) : Relation[Domain] =
		UNSAFE(host.path, relation)

	def apply[Domain : Manifest](rootHost : RemoteHost, query : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Relation[Domain] = {

		object Placement extends PlacementStrategy {
			val IR = idb.syntax.iql.IR
		}
		 val q = Placement.transform(root(query, rootHost))
		Predef.println(s"q = $q")
		globalDefsCache.toList.sortBy(e => e._1.id).foreach(Predef.println)

		val relation : Relation[Domain] = q
		val RemoteHost(_, queryPath) = q.host

		val ref = RemoteUtils.deploy(env.system, queryPath)(relation)
		val r = RemoteUtils.fromWithDeploy(env.system, ref)
		r
//		relation
	}
}
