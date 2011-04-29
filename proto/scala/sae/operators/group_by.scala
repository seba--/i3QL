package sae
package operators

import sae.collections._

import scala.collection.mutable.Map

case class group_by[V <: AnyRef, K <: AnyRef](val source: Observable[V], val groupKey: (V) => K)
   extends Observable[(K, Observable[V] /*The list of elements belonging to a specific group*/)]
   with Observer[V] {

   private class Count {
      private var count: Int = 0

      def inc() = { this.count += 1 }
      def dec(): Int = { this.count -= 1; this.count }

      def apply() = this.count
   }

   

   private var groups = Map[K, (Count, Observable[V])]()

   source.addObserver(this)

   def updated(oldV: V, newV: V) {
      val oldKey = groupKey(oldV)
      val newKey = groupKey(newV)
      if (oldKey == newKey) {
         val (_, group) = groups(oldKey)
         group.element_updated(oldV, newV);
      } else {
         removed(oldV);
         added(newV);
      }
   }

   def removed(v: V) {
      val key = groupKey(v)
      val (count, group) = groups(key)
      group.element_removed(v)
      if (count.dec == 0) {
         groups -= key
         element_removed(key, group)
      }
   }

   def added(v: V) {
      val key = groupKey(v);
      // (if necessary) signal that there is a new group
      val (count, group) = groups.getOrElseUpdate(key, {
         val observable = new Observable[V]() {}; // Observable basically serves as a means to push changes to different groups / to enable to attach listeners to the different groups
         element_added((key, observable)); (new Count(), observable)
      })
      count.inc
      group.element_added(v)
   }

   // the result is an observable list of pairs of values where the first
   // value is the key and the second value is the result of applying 
   // the function to the list..
   def per_group[T <: AnyRef](aggregateFuncConstr: (Observable[V]) => Observer[V] with Observable[T]): Observable[(K, Observable[T])] = {
      val per_group = new Observer[(K,Observable[V])] with Observable[(K, Observable[T])] {

         group_by.this addObserver this;
         
         private var groups =  Map[(K, Observable[V]),Observable[T]]()

         def added(v : (K,Observable[V])) {
        	val (key,value) = v
        	// attach a new instance of the aggregate observer to the group
        	val f = aggregateFuncConstr(value)
        	val group = (v,f);
            groups += (group)
            element_added((key,f))
         }

         def updated(oldV: (K,Observable[V]), newV: (K,Observable[V])) {
            // the list of observables will not be updated...
            throw new Error
         }

         def removed(v: (K,Observable[V])) {
        	 val f = groups(v)
        	 val key = v._1
        	 element_removed((key,f))
         }
    
      }

      per_group
   }
}