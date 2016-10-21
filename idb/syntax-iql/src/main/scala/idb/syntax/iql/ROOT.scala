package idb.syntax.iql

import akka.actor.ActorPath
import idb.query.{Host, QueryEnvironment, RemoteHost}
import idb.query.colors.Color
import idb.syntax.iql.IR._
import idb.syntax.iql.compilation.RemoteUtils

/**
 * @author Mirko Köhler
 */
object ROOT {

	def UNSAFE[Domain : Manifest](host : ActorPath, query : Rep[Query[Domain]])(implicit queryEnvironment : QueryEnvironment) : Relation[Domain] = {
		val relation : Relation[Domain] = query
		UNSAFE(host, relation)
	}

	def UNSAFE[Domain : Manifest](host : ActorPath, relation : Relation[Domain])(implicit queryEnvironment : QueryEnvironment) : Relation[Domain] = {
		val ref = RemoteUtils.deploy(queryEnvironment.system, host)(relation)
		val r = RemoteUtils.fromWithDeploy(queryEnvironment.system, ref)
		r
	}

	def UNSAFE[Domain : Manifest](host : RemoteHost, query : Rep[Query[Domain]])(implicit queryEnvironment : QueryEnvironment) : Relation[Domain] =
		UNSAFE(host.path, query)


	def UNSAFE[Domain : Manifest](host : RemoteHost, relation : Relation[Domain])(implicit queryEnvironment : QueryEnvironment) : Relation[Domain] =
		UNSAFE(host.path, relation)

	def apply[Domain : Manifest](rootHost : RemoteHost, query : Rep[Query[Domain]])(implicit queryEnvironment : QueryEnvironment) : Relation[Domain] = {
		val relation : Relation[Domain] = root(query, rootHost)
		val RemoteHost(_, queryPath) = query.host

		val ref = RemoteUtils.deploy(queryEnvironment.system, queryPath)(relation)
		val r = RemoteUtils.fromWithDeploy(queryEnvironment.system, ref)
		r
//		relation
	}
}
