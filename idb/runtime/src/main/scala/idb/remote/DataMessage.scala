package idb.remote

/**
 * Sealed trait to simplify pickling.
 */
sealed trait DataMessage extends Serializable
case class Added[V](d: V) extends DataMessage
case class Removed[V](d: V) extends DataMessage
case class Updated[V](oldV: V, newV: V) extends DataMessage
case class AddedAll[V](d: Seq[V]) extends DataMessage
case class RemovedAll[V](d: Seq[V]) extends DataMessage
