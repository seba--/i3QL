package sae
package operators

case class where[V <: AnyRef](val source: Observable[V], val test: (V) => Boolean) extends Operator[V] {

   source.addObserver(this)

   def select[Z <: AnyRef](f: (V) => Z) = new select(this, f)

   def updated(oldV: V, newV: V) {
      if (test(oldV)) {
         if (test(newV))
            element_updated(oldV, newV)
         else
            element_removed(oldV)
      } else if (test(newV)) {
         element_added(newV)
      }
   }

   def removed(k: V) {
      if (test(k))
         element_removed(k)
   }

   def added(k: V) {
      if (test(k))
         element_added(k)
   }
}