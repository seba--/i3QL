package idb.remote

import akka.actor.{Address, ActorSystem, ActorRef, Actor}
import idb.Relation
import idb.observer.Observable

object ObservableHost {
  sealed trait HostMessage
  case class HostObservableAndForward[T](obs: Observable[T], target: ActorRef)/*(implicit pickler: Pickler[T])*/ extends HostMessage
  case class Forward(target: ActorRef) extends HostMessage
  case class Host[T](obs: Observable[T]) extends HostMessage

  def forward(rel: Observable[_], actorSystem: ActorSystem): Unit = {
    rel match {
      case remoteView: RemoteView[_] => {
        val remoteHost = remoteView.remoteHost

        //TODO does this return the correct system?
        //val actorSystem = context.system
        val remoteViewActor = remoteView.createActor(actorSystem)

        remoteHost ! Forward(remoteViewActor)
      }
      case _ => rel.children.foreach { ch => forward(ch, actorSystem) }
    }
  }
}

class ObservableHost[T](var hosted: Option[Observable[T]] = None) extends Actor {
  import idb.remote.ObservableHost._

  override def receive = {
    // TODO: remove this case
    case HostObservableAndForward(obs: Observable[T], target) => {
      println("Now hosting " + obs.asInstanceOf[idb.Relation[T]].prettyprint(""))
      println(s"Target: ${target.toString()}, Self: ${context.self}")
      //target ! "Creating observer on remote host"
      obs.addObserver(new SendToRemote(target))
      println("Created new observer on remote host")
      hosted = Some(obs)
    }

    case Forward(target) =>
      hosted.get.addObserver(new SendToRemote(target));

    case Host(obs: Observable[T]) => {
      hosted = Some(obs)
      //TODO does this return the correct system?
      forward(obs, context.system)

      // answer the sender s.t. synchronization works
      sender() ! true
    }
  }

  def this(observable: Observable[T]) {
    this(Some(observable))
  }

  def this() {
    this(None)
  }
}