package idb.remote

import akka.actor.ActorRef

trait AccessMessage extends Serializable
case class SendTo(ref : ActorRef) extends AccessMessage
case object Initialize extends AccessMessage
case object Reset extends AccessMessage
case object Print extends AccessMessage
