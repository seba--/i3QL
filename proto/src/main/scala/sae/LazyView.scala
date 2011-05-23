package sae

/**
 * A lazy view is observable has a foreach that lazily
 * evaluates all tuples, possibly defering to the underlying relation
 * in the chain. Thus it can be Initialized for
 * tables that are already filled.
 * The lazy foreach may be a costly operation, but can also reduce the
 * amount of intermediate tables that are instantiated.
 */
trait LazyView[V <: AnyRef]
        extends Observable[V] 
{

    /**
     * Applies f to all elements of the view.
     * Implementors must guarantee that no update/add/remove event is
     * fired during the deferred iteration
     */
    def lazy_foreach[T](f : (V) => T)

        /**
     * Each materialized view must be able to
     * materialize it's content from the underlying
     * views.
     * The lazyness allows a query to be set up
     * on relations (tables) that are already filled.
     * thus the first call to foreach will try to
     * materialize already persisted data.
     */
    def lazyInitialize : Unit

    // record whether the initialization is complete
    protected var initialized : Boolean = false
}


class DefaultLazyView[V <: AnyRef]
        extends LazyView[V] 
{

    /**
     * Applies f to all elements of the view.
     * Implementors must guarantee that no update/add/remove event is
     * fired during the deferred iteration
     */
    def lazy_foreach[T](f : (V) => T) = {}

    
    def lazyInitialize : Unit = {}
}