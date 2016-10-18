package idb.syntax.iql.compilation

import akka.actor.{Actor, ActorRef, ActorSystem}
import idb.Relation
import idb.algebra.compiler.util.{BoxedEquiJoin, BoxedFunction}
import idb.observer.Observable
import idb.operators.impl.{EquiJoinView, ProjectionView, SelectionView, UnNestView}
import idb.remote._

/**
  * Created by Mirko on 13.09.2016.
  */
class RemoteActor[T](var hosted: Relation[T] = null) extends Actor {

	override def receive = {
		case ForwardMsg(target) =>
			new SendView(hosted, target);

		case HostMsg(obs: Relation[T]) =>
			hosted = obs
			RemoteActor.forward(context.system, obs)
			// answer the sender s.t. synchronization works
			sender() ! true

		case ResetMsg =>
			if (hosted != null) hosted.reset()
	}

	def this() = this(null)
}

object RemoteActor {

	def forward(system : ActorSystem, rel: Observable[_]): Unit = {
		rel match {
			case receive: ReceiveView[_] =>
				val remoteHost = receive.remoteActor
				val remoteViewActor = receive.withSystem(system)
				remoteHost ! ForwardMsg(receive.receiveActor)

			case r : SelectionView[_] =>
				BoxedFunction.compile(r.filter, CompilerBinding)

			case r : ProjectionView[_, _] =>
				BoxedFunction.compile(r.projection, CompilerBinding)

			case r : BoxedEquiJoin[_, _] =>
				r.compile(CompilerBinding)

			case r : UnNestView[_, _] =>
				BoxedFunction.compile(r.unNestFunction, CompilerBinding)

			case _ =>
		}

		rel.children.foreach(r => forward(system, r))
	}
}