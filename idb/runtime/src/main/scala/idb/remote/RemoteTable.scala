package idb.remote

import akka.actor.{ActorRef, Actor}
import idb.Table

class RemoteTable[V](table: Table[V]) extends Table[V] with Actor {

  override def receive = {
    case target: ActorRef => {
      table.addObserver(new SendToRemote(target));
    }
  }

  /**
    * Runtime information whether a compiled query is a set or a bag
    */
  override def isSet: Boolean = table.isSet



  //TODO is this proxy needed?

  override def update(oldV: V, newV: V) {
    table.update(oldV, newV)
  }

  override def ~=(vs: (V, V)): Table[V] = {
    table ~= vs
  }

  override def remove(v: V) {
    table.remove(v)
  }

  override def removeAll(vs: Seq[V]) {
    table.removeAll(vs)
  }

  override def -=(v: V): Table[V] = {
    table -= v
  }

  override def --=(vs: Seq[V]): Table[V] = {
    table --= vs
  }

  override def add(v: V): Unit = {
    table.add(v)
  }

  override def addAll(vs: Seq[V]) {
    table.addAll(vs)
  }

  override def +=(v: V): Table[V] = {
    table += v
  }

  override def ++=(vs: Seq[V]): Table[V] = {
    table ++= vs
  }

  // TODO not yet implemented
  override def prettyprint(implicit prefix: String) = table.prettyprint(prefix)


}
