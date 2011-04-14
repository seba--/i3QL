package sae
package operators

import java.lang.Integer

case class count[V <: AnyRef](val source: Observable[V]) extends Observer[V] with View[Integer] {

   // here, we need to store the current count value to make it updateable (we don't have access to the final materialized view by any means)
   private var value: Integer = new Integer(0)

   source.addObserver(this)

   def updated(oldV: V, newV: V) {
      // nothing to do
   }

   def removed(k: V) {
      element_updated(value, { value = new Integer(value.intValue - 1); value })
   }

   def added(k: V) {
      element_updated(value, new Integer({ value = value + 1; value }))
   }

   def foreach[T](f: (java.lang.Integer) => T) {
      f(value)
   }
}