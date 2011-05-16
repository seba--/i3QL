package sae
package operators

import sae.collections.BagResult

/**
 *
 */
object Conversions {

    def lazyViewToMaterializedView[V <: AnyRef](lazyView : LazyView[V]) : MaterializedView[V] =
        lazyView match {
            case view : LazySelection[V]    => new MaterializedSelection(view.filter, view.relation)
            case view : BagProjection[_, V] => new MaterializedBagProjection[view.Dom, V](view.projection, view.relation)
            case view : MaterializedView[V] => view
            case _                          => new BagResult(lazyView)
        }

    // internal class for forwarding 
    class HashIndexedViewProxy[V <: AnyRef](val relation : MaterializedView[V])
            extends IndexedView[V] {

        def lazyInitialize : Unit = {
            relation.lazyInitialize
        }
        
        def materialized_foreach[T](f : (V) => T) = relation.foreach(f)

        def materialized_size : Int = relation.size

        def materialized_singletonValue : Option[V] = relation.singletonValue
        
        protected def createIndex[K <: AnyRef](keyFunction : V => K) : Index[K, V] =
            new sae.collections.HashMultiMap[K, V](relation, keyFunction)
    }

    def lazyViewToIndexedView[V <: AnyRef](lazyView : LazyView[V]) : IndexedView[V] =
        lazyView match {
            case view : IndexedView[V]      => view
            case view : MaterializedView[V] => new HashIndexedViewProxy(view)
            case _                          => new HashIndexedViewProxy(lazyViewToMaterializedView(lazyView))
        }
}