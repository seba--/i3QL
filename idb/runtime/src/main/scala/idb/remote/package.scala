package idb

import akka.actor.{ActorPath, ActorRef, ActorSystem, Deploy, Props}
import akka.remote.RemoteScope
import idb.remote.receive.{PathRemoteReceiver, RemoteReceiver}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by mirko on 19.10.16.
  */
package object remote {

	/**
	  * Deploys a relation on the given node.
	  * @return A reference to an access actor that controls the given relation.
	  */
	def deploy[Domain](system : ActorSystem, node : ActorPath)(relation : Relation[Domain]) : ActorRef = {
		val ref = system.actorOf(
			Props(classOf[RelationActor[Domain]], relation)
				.withDeploy(Deploy(scope=RemoteScope(node.address)))
		)
		ref ! Initialize
		ref
	}

	/**
	  * Creates an for a relation, so that it can be referenced by other remotes.
	  * @return A reference to an access actor that controls the given relation.
	  */
	def create(system : ActorSystem)(id : String, relation : Relation[_]) : ActorRef = {
		system.actorOf(Props(classOf[RelationActor[_]], relation), id)
	}

	def from[Domain](system : ActorSystem)(path : ActorPath, timeout : FiniteDuration = 10 seconds) : RemoteReceiver[Domain] = {
//		val actorSelection = system.actorSelection(path).resolveOne(timeout)
//		val actorRef = Await.result(actorSelection, timeout)

		PathRemoteReceiver[Domain](path)
	}

	def fromWithDeploy[Domain](system : ActorSystem)(path : ActorPath, timeout : FiniteDuration = 10 seconds) : RemoteReceiver[Domain] = {
//		val actorSelection = system.actorSelection(path).resolveOne(timeout)
//		val actorRef = Await.result(actorSelection, timeout)

		val r = PathRemoteReceiver[Domain](path)
		r.deploy(system)
		r
	}

}
