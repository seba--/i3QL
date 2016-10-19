package idb.remote

/**
 * Sealed trait to simplify pickling.
 */
sealed trait DataEvent
case class Added[V](d: V) extends DataEvent
case class Removed[V](d: V) extends DataEvent
case class Updated[V](oldV: V, newV: V) extends DataEvent
case class AddedAll[V](d: Seq[V]) extends DataEvent
case class RemovedAll[V](d: Seq[V]) extends DataEvent
