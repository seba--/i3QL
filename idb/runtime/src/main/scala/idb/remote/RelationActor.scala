package idb.remote

import akka.actor.{Actor, ActorRef, Props}
import idb.Relation
import idb.observer.Observer
import idb.remote.receive.RemoteReceiver

import scala.collection.mutable

/**
  * Created by mirko on 18.10.16.
  */
class RelationActor[Domain](
	val relation : Relation[Domain]
) extends Actor with Observer[Domain] {
	relation.addObserver(this)

	private val registeredActors : mutable.Buffer[ActorRef] = mutable.Buffer.empty

	override def receive: Receive = {
		case SendTo(ref) =>
			println(s"RELATIONACTOR:SendTo($ref)")
			registeredActors += ref
			println(registeredActors)
		case Initialize =>
			println("RELATIONACTOR:Initialize")
			initialize(relation)
	}


	private def initialize(r : Relation[_]): Unit = {
		r match {
			case recv : RemoteReceiver[_] =>
				println("RELATIONACTOR:Deploy RemoteReceiver")
				recv.deploy(context.system)
			case _ =>
		}

		r.children.foreach(initialize)
	}

	override def updated(oldV: Domain, newV: Domain): Unit =
		registeredActors foreach (a => a ! Updated(oldV, newV))

	override def removed(v: Domain): Unit =
		registeredActors foreach (a => a ! Removed(v))

	override def removedAll(vs: Seq[Domain]): Unit =
		registeredActors foreach (a => a ! RemovedAll(vs))

	override def added(v: Domain): Unit =
		registeredActors foreach (a => a ! Added(v))

	override def addedAll(vs: Seq[Domain]): Unit =
		registeredActors foreach (a => a ! addedAll(vs))
}
