package idb.syntax.iql.compilation

import akka.actor.{ActorPath, ActorRef, ActorSystem, Deploy, Props}
import akka.remote.RemoteScope
import idb.Relation
import idb.algebra.compiler.boxing.{BoxedEquiJoin, BoxedFunction}
import idb.operators.impl.{ProjectionView, SelectionView, UnNestView}
import idb.remote.Initialize
import idb.remote.receive.{PathRemoteReceiver, RefRemoteReceiver, RemoteReceiver}


object RemoteUtils {
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
	  * Creates an actor for a relation, so that it can be referenced by other remotes.
	  * @return A reference to an access actor that controls the given relation.
	  */
	def create(system : ActorSystem)(id : String, relation : Relation[_]) : ActorRef = {
		system.actorOf(Props(classOf[RelationActor[_]], relation), id)
	}

	def from[Domain](path : ActorPath) : RemoteReceiver[Domain] = {
		PathRemoteReceiver[Domain](path)
	}

	def fromWithDeploy[Domain](system : ActorSystem, path : ActorPath) : RemoteReceiver[Domain] = {
		val r = from[Domain](path)
		r.deploy(system)
		r
	}

	def from[Domain](ref : ActorRef) : RemoteReceiver[Domain] = {
		RefRemoteReceiver[Domain](ref)
	}

	def fromWithDeploy[Domain](system : ActorSystem, ref : ActorRef) : RemoteReceiver[Domain] = {
		val r = from[Domain](ref)
		r.deploy(system)
		r
	}
}
