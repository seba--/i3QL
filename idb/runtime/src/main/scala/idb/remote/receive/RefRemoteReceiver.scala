package idb.remote.receive

import akka.actor.{ActorPath, ActorRef, ActorSystem}

import scala.concurrent.Await

/**
  * Created by mirko on 19.10.16.
  */
case class RefRemoteReceiver[Domain](actorRef : ActorRef) extends RemoteReceiver[Domain] {

	def deploy(system : ActorSystem): Unit = {
		internalDeploy(system, actorRef)
	}

}
