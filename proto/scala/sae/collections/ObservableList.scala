package sae
package collections

import scala.collection.mutable.ListBuffer

class ObservableList[V <: AnyRef] extends View[V] {

//   protected var data = List[V]();
   protected var data = ListBuffer[V]();

   def add(k: V) {
//      data += k 
      element_added(k)
   }

   def remove(k: V) {
      //data = data.filterNot(_ eq k)
  //   data -= k
      element_removed(k)
   }

   def update(oldV: V, newV: V) {
      //data = newV +: (data.filterNot(_ eq oldV))
     data -= oldV
     data += newV
     element_updated(oldV, newV)
   }

   def foreach[T](f: (V) => T) {
      data.foreach(f)
   }

}

