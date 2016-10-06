package idb.syntax.iql.compilation

import akka.actor.{Actor, ActorRef, ActorSystem}
import idb.Relation
import idb.algebra.compiler.util.BoxedFunction
import idb.observer.Observable
import idb.operators.impl.{ProjectionView, SelectionView}
import idb.remote._

/**
  * Created by Mirko on 13.09.2016.
  */
class RemoteActor[T](var hosted: Relation[T] = null) extends Actor {

	override def receive = {
		case ForwardMsg(target) =>
			hosted.addObserver(new Send(target));

		case HostMsg(obs: Relation[T]) =>
			hosted = obs
			RemoteActor.forward(context.system, obs)
			// answer the sender s.t. synchronization works
			sender() ! true

		case ResetMsg =>
			if (hosted != null) hosted._reset()
	}

	def this() = this(null)
}

object RemoteActor {

	def forward(system : ActorSystem, rel: Observable[_]): Unit = {
		rel match {
			case receive: Receive[_] =>
				val remoteHost = receive.remoteActor
				val remoteViewActor = receive.withSystem(system)
				remoteHost ! ForwardMsg(receive.receiveActor)

			case r : SelectionView[_] =>
				r.filter match {
					case b@BoxedFunction(_) =>
						b.compile(CompilerBinding)
					case _ =>
				}
				r.children.foreach(child => forward(system, child))

			case r : ProjectionView[_, _] =>
				r.projection match {
					case b@BoxedFunction(_) =>
						b.compile(CompilerBinding)
					case _ =>
				}
				r.children.foreach(child => forward(system, child))

			case _ => rel.children.foreach(r => forward(system, r))
		}
	}
}