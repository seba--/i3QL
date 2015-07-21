package idb.remote

trait Message
case class Added[V](d: V) extends Message
case class Removed[V](d: V) extends Message
case class Updated[V](oldV: V, newV: V) extends Message
case class AddedAll[V](d: Seq[V]) extends Message
case class RemovedAll[V](d: Seq[V]) extends Message
case object EndTransaction extends Message
