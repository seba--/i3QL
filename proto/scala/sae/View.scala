package sae

trait View[V <: AnyRef] extends Observable[V] {
   def foreach[T](f: (V) => T)
}