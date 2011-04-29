package sae
package operators

import sae.collections._

case class select[V <: AnyRef, Z <: AnyRef](val source: Observable[V], val f: (V) => Z) extends Observable[Z] with Observer[V] {

   source.addObserver(this)

   def updated(oldV: V, newV: V) {
      element_updated(f(oldV), f(newV))
   }

   def removed(v: V) {
      element_removed(f(v))
   }

   def added(v: V) {
      element_added(f(v))
   }

   def as_view() = {
	   new ViewList(this)
   }
   
}