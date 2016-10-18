package idb.remote

import akka.actor.ActorRef
import idb.Relation
import idb.observer.Observer

/**
  * An observer that forwards all events as messages to a possibly
  * remote actor.
  *
  * Later `SendToRemote` will need to have a `Pickler[Domain]`, so that
  * it can pickle values before they are sent to the `actor`.
  */
class SendView[Domain](val relation : Relation[Domain], val actor : ActorRef) extends Observer[Domain] {
	relation.addObserver(this)

	override def added(v: Domain) =
		actor ! Added(v)

	override def removed(v: Domain) =
		actor ! Removed(v)

	override def updated(oldV: Domain, newV: Domain) =
		actor ! Updated(oldV, newV)

	override def addedAll(vs: Seq[Domain]) =
		actor ! AddedAll(vs)

	override def removedAll(vs: Seq[Domain]) =
		actor ! RemovedAll(vs)
}
