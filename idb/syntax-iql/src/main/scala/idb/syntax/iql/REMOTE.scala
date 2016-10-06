package idb.syntax.iql

import akka.actor.{ActorPath, ActorRef, Props}
import idb.query.colors.Color
import idb.{BagTable, Table}
import idb.query.{Host, QueryEnvironment, RemoteHost}
import idb.remote.Receive
import idb.syntax.iql.IR._
import idb.syntax.iql.compilation.RemoteActor

/**
  * Created by Mirko on 06.09.2016.
  */
object REMOTE {

	def RELATION[V](table : Relation[V], id : String)(implicit queryEnvironment : QueryEnvironment) : ActorRef = {
		queryEnvironment.system.actorOf(Props(classOf[RemoteActor[V]], table), id)
	}

	def FROM[Domain : Manifest] (host : RemoteHost, id : String, color : Color)(implicit queryEnvironment : QueryEnvironment) : Rep[Query[Domain]] = {
		val remoteHostPath: ActorPath = host.path / "user" / id
		relation[Domain](
			Receive[Domain](queryEnvironment.system, remoteHostPath, false),
			color = color,
			host = host
		)
	}

}
