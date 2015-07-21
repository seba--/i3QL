package idb.observer

trait Message[V]
case class Add[V](d: V) extends Message[V]
case class Remove[V](d: V) extends Message[V]
case class Update[V](oldV: V, newV: V) extends Message[V]
case class AddAll[V](d: Seq[V]) extends Message[V]
case class RemoveAll[V](d: Seq[V]) extends Message[V]
