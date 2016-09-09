package idb.syntax.iql

import akka.actor.{ActorPath, ActorRef, Props}
import idb.query.colors.Color
import idb.{BagTable, Table}
import idb.query.{Host, QueryEnvironment, RemoteHost}
import idb.remote.{ObservableHost, RemoteView}
import idb.syntax.iql.IR._

/**
  * Created by Mirko on 06.09.2016.
  */
object REMOTE {

	def TABLE[V] (table : Table[V], id : String)(implicit queryEnvironment : QueryEnvironment) : ActorRef = {
		queryEnvironment.actorSystem.actorOf(Props(classOf[ObservableHost[V]], table), id)
	}

	def FROM[Domain : Manifest] (host : RemoteHost, id : String, color : Color)(implicit queryEnvironment : QueryEnvironment) : Rep[Query[Domain]] = {
		val remoteHostPath: ActorPath = host.path / "user" / id
		relation[Domain](
			RemoteView[Domain](queryEnvironment.actorSystem, remoteHostPath, false),
			color = color,
			host = host
		)
	}

}
