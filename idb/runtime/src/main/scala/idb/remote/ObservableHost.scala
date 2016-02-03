package idb.remote

import akka.actor.{Address, ActorSystem, ActorRef, Actor}
import idb.Relation
import idb.observer.Observable

object ObservableHost {
  sealed trait HostMessage
  case class HostObservableAndForward[T](obs: Observable[T], target: ActorRef)/*(implicit pickler: Pickler[T])*/ extends HostMessage
  case class Forward(target: ActorRef) extends HostMessage
  case class Host[T](obs: Observable[T]) extends HostMessage
  //case class DoIt[T](fun: Observable[T] => Unit) extends HostMessage


}

class ObservableHost[T](var hosted: Option[Observable[T]] = None) extends Actor {
  import idb.remote.ObservableHost._

  override def receive = {
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
      walk(obs)
    }

    /*case DoIt(fun: (Observable[T] => Unit)) =>
      fun(hosted.get)*/
  }

  def this(observable: Observable[T]) {
    this(Some(observable))
  }

  def this() {
    this(None)
  }

  def walk(actorSystem: ActorSystem /*TODO: Where to get this from?*/, rel: Relation[_], address: Option[Address] = None): Unit = {
    rel match {
      case remoteView: RemoteView[_] => {
        val remoteHost = remoteView.remoteHost
        val remoteViewActor = remoteView.createActor(actorSystem)
        remoteHost ! Forward(remoteViewActor)
      }
      case _ => rel.children.foreach { ch => walk(actorSystem, ch, address) }
    }
  }
}