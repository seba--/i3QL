package idb.syntax.iql

import akka.actor.{ActorPath, ActorRef}
import idb.query.colors.Color
import idb.query.{Host, QueryEnvironment, RemoteHost}
import idb.remote
import idb.syntax.iql.IR._
import idb.syntax.iql.compilation.RemoteUtils


/**
  * Created by Mirko on 06.09.2016.
  */
object REMOTE {

	def DEFINE[V](table : Relation[V], id : String)(implicit queryEnvironment : QueryEnvironment) : ActorRef = {
		//The created actor has the path host.path / "user" / id
		RemoteUtils.create(queryEnvironment.system)(id, table)
	}

	def GET[Domain : Manifest](host : RemoteHost, id : String, color : Color = Color.NO_COLOR)(implicit queryEnvironment : QueryEnvironment) : Rep[Query[Domain]] = {
		val actorPath : ActorPath = host.path / "user" / id

		reclassification(
			actorDef[Domain](actorPath, host),
			color
		)
	}



}
