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

}