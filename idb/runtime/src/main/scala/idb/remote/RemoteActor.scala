package idb.remote

import akka.actor.{Address, ActorSystem, ActorRef, Actor}
import idb.Relation
import idb.observer.Observable



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
      case _ => rel.children.foreach(r => forward(system, r))
    }
  }
}