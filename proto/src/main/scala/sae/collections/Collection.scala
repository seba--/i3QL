package sae
package collections

trait Collection[V <: AnyRef]
        extends MaterializedView[V]
        with Result[V] {

    def size : Int = {
        if (!initialized) {
            lazyInitialize
            initialized = true
        }
        materialized_size
    }

    def materialized_size : Int

    def singletonValue : Option[V] = {
        if (!initialized) {
            lazyInitialize
            initialized = true
        }
        materialized_singletonValue
    }

    def materialized_singletonValue : Option[V]

    /**
     * Add a data tuple to this relation.
     * The element is added and then a change event is fired.
     */
    def +=(v : V) : Collection[V] =
        {
            add_element(v)
            element_added(v)
            this
        }

    /**
     * Internal implementation of the add method
     */
    protected def add_element(v : V) : Unit

    /**
     * Remove a data tuple from this relation
     * The element is removed and then a change event is fired.
     */
    def -=(v : V) : Collection[V] =
        {
            remove_element(v)
            element_removed(v)
            this
        }

    /**
     * internal implementation remove method
     */
    protected def remove_element(v : V) : Unit

}