package idb.remote.receive

import akka.actor.{ActorPath, ActorRef, ActorSystem}

import scala.concurrent.Await

/**
  * Created by mirko on 19.10.16.
  */
case class PathRemoteReceiver[Domain](actorPath : ActorPath) extends RemoteReceiver[Domain] {

	def deploy(system : ActorSystem): ActorRef = {
		//Creates a new receive actor and adds this relation to be notified
		val actorSelection = system.actorSelection(actorPath).resolveOne(timeout)
		val actorRef = Await.result(actorSelection, timeout)
		internalDeploy(system, actorRef)
	}

}
