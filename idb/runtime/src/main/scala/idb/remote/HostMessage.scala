package idb.remote

import akka.actor.ActorRef
import idb.Relation
import idb.observer.Observable

/**
  * Created by mirko on 06.10.16.
  */

sealed trait HostMessage
case class ForwardMsg(target: ActorRef) extends HostMessage
case class HostMsg[T](obs: Relation[T]) extends HostMessage
case object ResetMsg extends HostMessage

