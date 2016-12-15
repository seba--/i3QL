package idb.syntax.iql

import akka.actor.{ActorPath, ActorRef}
import idb.query.taint.Taint
import idb.query.{Host, QueryEnvironment, RemoteHost}
import idb.remote
import idb.syntax.iql.IR._
import idb.syntax.iql.compilation.RemoteUtils


/**
  * Use this command to define and retrieve data for/from remote hosts.
  */
object REMOTE {

	/**
	  * Defines a new remote actor. Its data can be retrieved from other hosts.
	  * @param relation The relation which data should be exposed.
	  * @param id The name for the remote definition which is needed to address this remote actor.
	  * @return Returns the reference of the created remote actor.
	  *
	  * @see{idb.syntax.iql.REMOTE.GET}
	  */
	def DEFINE[V](relation : Relation[V], id : String)(implicit env : QueryEnvironment) : ActorRef = {
		//The created actor has the path host.path / "user" / id
		RemoteUtils.create(env.system)(id, relation)
	}

	/**
	  * Retrieves data from a remote actor.
	  * @param host The host on which the remote actor is deployed.
	  * @param id The name of the remote actor.
	  * @param taint The taint that should be used for ndata from this remote actor.
	  * @return A query that has the data for this remote actor.
	  *
	  * @see{idb.syntax.iql.REMOTE.DEFINE}
	  */
	def GET[Domain : Manifest](host : RemoteHost, id : String, taint : Taint = Taint.NO_TAINT)(implicit env : QueryEnvironment) : Rep[Query[Domain]] = {
		val actorPath : ActorPath = host.path / "user" / id
		actorDef[Domain](actorPath, host, taint)
	}



}
