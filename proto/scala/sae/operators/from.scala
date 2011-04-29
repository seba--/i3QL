package sae
package operators

case class from[V <: AnyRef](val source: Observable[V]) extends Operator[V] {

   source.addObserver(this)

   def updated(oldV: V, newV: V) {
      element_updated(oldV, newV)
   }

   def removed(v: V) {
      element_removed(v)
   }

   def added(v: V) {
      element_added(v)
   }

   // used for constructing the query/the AST
   def where(test: (V) => Boolean) = new where(this, test)

   def group_by[GroupKey <: AnyRef](grouping: (V) => GroupKey) = new group_by(this, grouping)

}
