package idb.remote.receive

import akka.actor.{ActorRef, ActorSystem, Props}
import idb.Relation
import idb.observer.{NotifyObservers, Observer}
import idb.remote.SendTo

import scala.language.postfixOps

/**
  * Created by mirko on 18.10.16.
  */
trait RemoteReceiver[Domain] extends Relation[Domain] with NotifyObservers[Domain] with Observer[Domain] {


	import scala.concurrent.duration._
	val timeout = 10 seconds

	def deploy(system : ActorSystem): ActorRef

	protected def internalDeploy(system : ActorSystem, remoteRef : ActorRef): ActorRef = {
		val ref = system.actorOf(Props(classOf[ReceiveActorAdapter[Domain]], this))
		remoteRef ! SendTo(ref)
		ref
	}

	/**
	  * Deploys this receiver and returns the actor that is used to receive data events.
	  */


	override def isSet: Boolean = false

	override def foreach[T](f: (Domain) => T): Unit = {}

	override def children: Seq[Relation[_]] = Nil

	override protected def resetInternal(): Unit = {}

	override def prettyprint(implicit prefix: String): String = s"Receiver{$this}"


	override def updated(oldV: Domain, newV: Domain): Unit = {
		notify_updated(oldV, newV)
	}

	override def removed(v: Domain): Unit = {
		notify_removed(v)
	}

	override def removedAll(vs: Seq[Domain]): Unit = {
		notify_removedAll(vs)
	}

	override def added(v: Domain): Unit = {
		notify_added(v)
	}

	override def addedAll(vs: Seq[Domain]): Unit = {
		notify_addedAll(vs)
	}
}
