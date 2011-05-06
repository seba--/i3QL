package sae

/**
 * This view materializes its elements and thus requires
 * some form of intermediate storage.
 */
trait MaterializedView[V <: AnyRef]
        extends View[V]
        with LazyView[V] {

    /**
     * A materialized view never needs to defer
     * since it recored it's own elements
     */
    def lazy_foreach[T](f : (V) => T) = foreach(f)

    /**
     * Each materilaized view must be able to 
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

    /**
     * We give an abstract implementation of foreach, with
     * lazy initialization semantics.
     * But clients are required to implement their own 
     * foreach method, with concrete semantics.
     */
    def foreach[T](f : (V) => T) : Unit =
        {
            if (!initialized) {
                lazyInitialize
                initialized = true
            }
            materialized_foreach(f)
        }
    
    /**
     * The internal implementation that iterates only over materialized
     * data. 
     */
    protected def materialized_foreach[T](f : (V) => T) : Unit
}