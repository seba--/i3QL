package idb.syntax.iql.compilation

import akka.actor.{Actor, ActorRef, ActorSystem}
import idb.Relation
import idb.algebra.compiler.boxing.BoxedFunction
import idb.algebra.compiler.boxing.BoxedEquiJoin
import idb.observer.{NotifyObservers, Observer}
import idb.operators.impl.{ProjectionView, SelectionView, UnNestView}
import idb.remote._
import idb.remote.receive.RemoteReceiver

import scala.collection.mutable

/**
  * Created by mirko on 18.10.16.
  */
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
			relation.print()
	}

	def initialize(relation : Relation[_]): Unit = {

		relation match {
			case r : SelectionView[_] =>
				BoxedFunction.compile(r.filter, CompilerBinding)

			case r : ProjectionView[_, _] =>
				BoxedFunction.compile(r.projection, CompilerBinding)

			case r : BoxedEquiJoin[_, _] =>
				r.compile(CompilerBinding)

			case r : UnNestView[_, _] =>
				BoxedFunction.compile(r.unNestFunction, CompilerBinding)

			case recv : RemoteReceiver[_] =>
				recv.deploy(context.system)

			case _ =>
		}

		relation.children.foreach(c => initialize(c))
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
