package idb.syntax.iql.compilation

import akka.actor.{Actor, ActorRef, ActorSystem}
import idb.Relation
import idb.algebra.compiler.boxing.{BoxedAggregationNotSelfMaintained, BoxedAggregationSelfMaintained, BoxedEquiJoin, BoxedFunction}
import idb.observer.{NotifyObservers, Observer}
import idb.operators.impl.{ProjectionView, SelectionView, UnNestView}
import idb.remote._
import idb.remote.receive.RemoteReceiver

import scala.collection.mutable

//TODO: Is there a way to move this class to idb-runtime instead?
class RelationActor[Domain](
	val relation : Relation[Domain]
) extends Actor with Observer[Domain] {
	relation.addObserver(this)

	private val registeredActors : mutable.Buffer[ActorRef] = mutable.Buffer.empty

	override def receive: Receive = {
		case SendTo(ref) =>
			println(s"[RelationActor] Adding link: ${this.self.path} ---> ${ref.path}")
			registeredActors += ref
		case Initialize =>
			initialize(relation)
			println(s"[RelationActor] Initialized ${this.self}")

		case Reset =>
			println(s"[RelationActor] Reset ${this.self}")
			relation.reset()
		case Print =>
			val out = System.out //TODO: How to choose the correct printstream here?
			out.println(s"Actor[${self.path.toStringWithoutAddress}]{")
			relation.printNested(out, relation)(" ")
			out.println(s"}")
	}

	def initialize(relation : Relation[_]): Unit = {

		relation match {
			case recv : RemoteReceiver[_] =>
				recv.deploy(context.system)
		}

		CompilerBinding.initialize(relation)
	}

	override def updated(oldV: Domain, newV: Domain): Unit ={

		registeredActors foreach (a => a ! Updated(oldV, newV))
	}


	override def removed(v: Domain): Unit =
		registeredActors foreach (a => a ! Removed(v))

	override def removedAll(vs: Seq[Domain]): Unit =
		registeredActors foreach (a => a ! RemovedAll(vs))

	override def added(v: Domain): Unit = {

		registeredActors foreach (a => {
			a ! Added(v)
		})
	}


	override def addedAll(vs: Seq[Domain]): Unit = {
		registeredActors foreach (a => {
			a ! AddedAll(vs)
		})
	}

}
