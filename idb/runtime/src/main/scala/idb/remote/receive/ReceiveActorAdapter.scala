package idb.remote.receive

import akka.actor.Actor
import idb.observer.{NotifyObservers, Observable, Observer}
import idb.remote._

/**
  * Created by mirko on 19.10.16.
  */
class ReceiveActorAdapter[Domain](observers : Seq[Observer[Domain]]) extends Actor with Observable[Domain] with NotifyObservers[Domain] {

	def this(obs : Observer[Domain]) = this(Seq(obs))

	observers.foreach(addObserver)

	override def receive = {
		case Added(v: Domain) =>
//			println(s"$this#Added[$v]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			notify_added(v)
		case Removed(v: Domain) =>
//			println(s"$this#Removed[$v]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			notify_removed(v)
		case Updated(oldV: Domain, newV: Domain) =>
//			println(s"$this#Updated[$oldV=>$newV]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			notify_updated(oldV, newV)
		case AddedAll(vs: Seq[Domain]) =>
//			println(s"$this#AddedAll[$vs]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			notify_addedAll(vs)
		case RemovedAll(vs: Seq[Domain]) =>
//			println(s"$this#RemovedAll[$vs]--${sender().path.toStringWithoutAddress}-->${context.self.path.toStringWithoutAddress}")
			notify_removedAll(vs)
	}

	/**
	  * Returns the observed children, to allow a top down removal of observers
	  */
	override def children: Seq[Observable[_]] = Nil
}
