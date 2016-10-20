package idb.syntax.iql.compilation

import akka.actor.{Actor, ActorRef}
import idb.Relation
import idb.algebra.compiler.boxing.BoxedFunction
import idb.algebra.compiler.boxing.BoxedEquiJoin
import idb.observer.Observer
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
			println(s"[RelationActor] Initialize ${this.self}")
			initialize(relation)
	}


	private def initialize(relation : Relation[_]): Unit = {
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
				val ref = recv.deploy(context.system)
				println(s"[RelationActor] Deployed $ref on ${this.self}")
			case _ =>
		}

		relation.children.foreach(initialize)
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
