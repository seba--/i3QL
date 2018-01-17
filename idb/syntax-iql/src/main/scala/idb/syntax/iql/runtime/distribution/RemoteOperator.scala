package idb.syntax.iql.runtime.distribution

import akka.actor.{Actor, ActorRef}
import idb.Relation
import idb.algebra.compiler.boxing.{BoxedAggregationNotSelfMaintained, BoxedAggregationSelfMaintained, BoxedEquiJoin, BoxedFunction}
import idb.observer.Observer
import idb.operators.impl.{ProjectionView, SelectionView, UnNestView}
import idb.remote._
import idb.remote.receive.RemoteReceiver
import idb.syntax.iql.runtime.CompilerBinding

import scala.collection.mutable

//TODO: Is there a way to move this class to idb-runtime instead?
class RemoteOperator[Domain](
	val relation : Relation[Domain]
) extends Actor with Observer[Domain] {
	relation.addObserver(this)

	private val registeredActors : mutable.Buffer[ActorRef] = mutable.Buffer.empty

	override def receive: Receive = {
		case SendTo(ref) =>
			registeredActors += ref
		case Initialize =>
			initialize(relation)

		case Reset =>
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

			case r : SelectionView[_] =>
				BoxedFunction.compile(r.filter, CompilerBinding)

			case r : ProjectionView[_, _] =>
				BoxedFunction.compile(r.projection, CompilerBinding)

			case r : BoxedEquiJoin[_, _] =>
				r.compile(CompilerBinding)

			case r : BoxedAggregationSelfMaintained[_, _, _, _, _] =>
				r.compile(CompilerBinding)

			case r : BoxedAggregationNotSelfMaintained[_, _, _, _, _] =>
				r.compile(CompilerBinding)

			case r : UnNestView[_, _] =>
				BoxedFunction.compile(r.unNestFunction, CompilerBinding)

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
