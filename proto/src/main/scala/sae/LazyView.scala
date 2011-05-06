package sae

/**
 * A lazy view is observable and initializes itself lazily
 * on the first call to foreach. Thus it can be Initialized for
 * tables that are already filled.
 */
trait LazyView[T <: AnyRef]
        extends View[T] {

    /**
     * Initializes this view.
     * Implementors must guarantee that no update/add/remove event is fired during the materialization
     */
    protected def lazyInitialize()

}