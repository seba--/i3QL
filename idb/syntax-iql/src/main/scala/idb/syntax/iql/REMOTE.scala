package idb.syntax.iql

import akka.actor.{ActorPath, ActorRef}
import idb.query.colors.Color
import idb.query.{QueryEnvironment, RemoteHost}
import idb.remote
import idb.syntax.iql.IR._


/**
  * Created by Mirko on 06.09.2016.
  */
object REMOTE {

	def DEFINE[V](table : Relation[V], id : String)(implicit queryEnvironment : QueryEnvironment) : ActorRef = {
		idb.remote.create(queryEnvironment.system)(id, table)
	}

	def GET[Domain : Manifest](host : ActorPath, id : String, color : Color)(implicit queryEnvironment : QueryEnvironment) : Relation[Domain] = {
		val remoteHostPath: ActorPath = host / "user" / id
		idb.remote.from[Int](remoteHostPath)
	}

}
