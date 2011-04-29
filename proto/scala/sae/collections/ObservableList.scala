package sae
package collections

class ObservableList[V <: AnyRef] extends View[V] {

   protected var data = List[V]();

   def add(k: V) {
      data = k +: data
      element_added(k)
   }

   def remove(k: V) {
      data = data.filterNot(_ eq k)
      element_removed(k)
   }

   def update(oldV: V, newV: V) {
      data = newV +: (data.filterNot(_ eq oldV))
      element_updated(oldV, newV)
   }

   def foreach[T](f: (V) => T) {
      data.foreach(f)
   }

}

