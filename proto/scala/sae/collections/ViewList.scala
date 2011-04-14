package sae
package collections

class ViewList[V <: AnyRef](val o: Observable[V]) extends Observer[V] with View[V] {

   o addObserver this

   protected var data = List[V]();

   def added(v: V) { add(v) }
   def removed(v: V) { remove(v) }
   def updated(oldV: V, newV: V) { update(oldV, newV) }

   def add(k: V) {
      data = k +: data
      element_added(k)
   }

   def remove(k: V) {
      data = data.filterNot(_ == k)
      element_removed(k)
   }

   def update(oldV: V, newV: V) {
      data = newV +: (data.filterNot(_ == oldV))
      element_updated(oldV, newV)
   }

   def foreach[T](f: (V) => T) {
      data.foreach(f)
   }

}

