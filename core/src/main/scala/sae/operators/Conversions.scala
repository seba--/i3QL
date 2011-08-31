package sae
package operators

import sae.collections.BagResult

/**
 *
 */
object Conversions {

  	// note rewrite to specialized operators makes sense, but may also 
    // be irritating from a client perspective.
    // if you use viewX in a query and want to add elements later, then maybe viewX 
    // was replaced inside a query and results do not get updated
    def lazyViewToMaterializedView[V <: AnyRef](lazyView : LazyView[V]) : MaterializedView[V] =
        lazyView match { 
            // case view : LazySelection[V]    => new MaterializedSelection(view.filter, view.relation)
            // case view : BagProjection[_, V] => new MaterializedBagProjection[view.Dom, V](view.projection, view.relation)
            case view : MaterializedView[V] => view
            case _                          => new BagResult(lazyView)
        }

    // internal class for forwarding 
    class HashIndexedViewProxy[V <: AnyRef](val relation : MaterializedView[V])
            extends IndexedView[V]
            with ObservableProxy[V]
    {

        initialized = true

        def lazyInitialize
        {
            // do nothing
        }
        
        def materialized_foreach[T](f : (V) => T)
        {
            relation.foreach(f)
        }

        def materialized_size : Int = relation.size

        def materialized_singletonValue : Option[V] = relation.singletonValue
        
        protected def createIndex[K <: AnyRef](keyFunction : V => K) : Index[K, V] =
            new sae.collections.HashBagIndex[K, V](relation, keyFunction)

        protected def materialized_contains(v: V) = relation.contains(v)
    }

    def lazyViewToIndexedView[V <: AnyRef](lazyView : LazyView[V]) : IndexedView[V] =
        lazyView match {
            case view : IndexedView[V]      => view
            case view : MaterializedView[V] => new HashIndexedViewProxy(view)
            case _                          => new HashIndexedViewProxy(lazyViewToMaterializedView(lazyView))
        }

}