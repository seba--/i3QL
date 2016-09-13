package idb.syntax.iql.compilation

import akka.actor.{Actor, ActorRef, ActorSystem}
import idb.algebra.compiler.util.BoxedFunction
import idb.observer.Observable
import idb.operators.impl.{ProjectionView, SelectionView}
import idb.remote.{Receive, Send}

/**
  * Created by Mirko on 13.09.2016.
  */
class RemoteActor[T](var hosted: Observable[T] = null) extends Actor {

	override def receive = {
		case Forward(target) =>
			hosted.addObserver(new Send(target));

		case Host(obs: Observable[T]) =>
			hosted = obs
			RemoteActor.forward(context.system, obs)
			// answer the sender s.t. synchronization works
			sender() ! true
	}

	def this() = this(null)
}

sealed trait HostMessage
case class Forward(target: ActorRef) extends HostMessage
case class Host[T](obs: Observable[T]) extends HostMessage

object RemoteActor {

	def forward(system : ActorSystem, rel: Observable[_]): Unit = {
		rel match {
			case receive: Receive[_] =>
				val remoteHost = receive.remoteActor
				val remoteViewActor = receive.withSystem(system)
				remoteHost ! Forward(receive.receiveActor)

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