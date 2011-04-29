package sae

trait Observer[-V] {

   def updated(oldV: V, newV: V): Unit

   def removed(v: V): Unit

   def added(v: V): Unit

}