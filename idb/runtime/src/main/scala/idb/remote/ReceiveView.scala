package idb.remote

import akka.actor.{Actor, ActorPath, ActorRef, ActorSystem, Address, Deploy, Props}
import akka.remote.RemoteScope
import akka.util.Timeout
import idb.Relation
import idb.observer.NotifyObservers

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * Created by Mirko on 13.09.2016.
  */
class ReceiveView[Domain](
	val remoteActor : ActorRef,
	val isSet : Boolean = false
) extends Relation[Domain] with NotifyObservers[Domain] {
	//Initialize a new actor that is able to receive Added/Removed messages
	var receiveActor : ActorRef = _

	def withSystem(system : ActorSystem): Unit = {
		receiveActor = system.actorOf(Props(classOf[ReceivingActor[Domain]], this))
	}

	override def foreach[T](f: (Domain) => T): Unit = { }

	override protected def lazyInitialize(): Unit = { }

	override def children: Seq[Relation[_]] = Nil

	override def prettyprint(implicit prefix: String): String = s"Recieve($remoteActor)"

	//make notify methods public
	override def notify_added(v: Domain): Unit = super.notify_added(v)
	override def notify_removed(v: Domain): Unit = super.notify_removed(v)
	override def notify_updated(oldV: Domain, newV: Domain): Unit = super.notify_updated(oldV, newV)
	override def notify_addedAll(vs: Seq[Domain]): Unit = super.notify_addedAll(vs)
	override def notify_removedAll(vs: Seq[Domain]): Unit = super.notify_removedAll(vs)
	override def notify_endTransaction(): Unit = super.notify_endTransaction()

	override protected def resetInternal(): Unit = {
		remoteActor ! ResetMsg
	}
}


class ReceivingActor[Domain](recv : ReceiveView[Domain]) extends Actor {
	override def receive = {
		case Added(v: Domain) =>
			println(s"$this#Added[$v]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			recv.notify_added(v)
		case Removed(v: Domain) =>
			println(s"$this#Removed[$v]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			recv.notify_removed(v)
		case Updated(oldV: Domain, newV: Domain) =>
			println(s"$this#Updated[$oldV=>$newV]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			recv.notify_updated(oldV, newV)
		case AddedAll(vs: Seq[Domain]) =>
			println(s"$this#AddedAll[$vs]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			recv.notify_addedAll(vs)
		case RemovedAll(vs: Seq[Domain]) =>
			println(s"$this#RemovedAll[$vs]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			recv.notify_removedAll(vs)
		case EndTransaction =>
			println(s"$this#EndTransaction--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			recv.notify_endTransaction()
		//case str: String => println(s"DEBUG (sender: ${sender()}, self: ${context.self}): $str")
	}

}

object ReceiveView {
	def apply[T](system : ActorSystem, remoteHostPath : ActorPath, isSet : Boolean = false) = {
		val remoteHost = system.actorSelection(remoteHostPath)
		new ReceiveView[T] (
			Await.result(remoteHost.resolveOne()(Timeout(60 second)), 60 second),
			isSet
		)
	}

	def apply[T](system: ActorSystem, remoteAddr: Address, partition: Relation[T]): ReceiveView[T] = {
		/*val remoteHost = system.actorOf(Props(classOf[RemoteActor[T]]).withDeploy(Deploy(scope=RemoteScope(remoteAddr))))

		val receive = new Receive[T](remoteHost, partition.isSet)

		// synchronize Host message
		import akka.pattern.ask //imports the ?
		implicit val timeout = Timeout(10 seconds)
		val res = remoteHost ? Host(partition)
		Await.result(res, timeout.duration)

		receive*/
		??? //Do not implement!
	}
}
