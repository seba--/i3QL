package idb.syntax.iql

import akka.actor.{ActorPath, ActorRef, Props}
import idb.query.colors.Color
import idb.{BagTable, Table}
import idb.query.{Host, QueryEnvironment, RemoteHost}
import idb.remote.RelationActor
import idb.syntax.iql.IR._

/**
  * Created by Mirko on 06.09.2016.
  */
object REMOTE {

	def DEFINE[V](table : Relation[V], id : String)(implicit queryEnvironment : QueryEnvironment) : ActorRef = {
		queryEnvironment.system.actorOf(Props(classOf[RelationActor[_]], table), id)
	}

	def GET[Domain : Manifest](host : RemoteHost, id : String, color : Color)(implicit queryEnvironment : QueryEnvironment) : Rep[Query[Domain]] = {
		val remoteHostPath: ActorPath = host.path / "user" / id

		actorDef[Domain](remoteHostPath, host)

//		//FIXME: This should be changed to not be added on the compiling node
//		relation[Domain](
//			ReceiveView[Domain](queryEnvironment.system, remoteHostPath, false),
//			color = color,
//			host = host
//		)
	}

}
