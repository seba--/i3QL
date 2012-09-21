package sae
package collections

trait Collection[V]
    extends MaterializedRelation[V]
{

    /**
     * Add a data tuple to this relation.
     * The element is added and then a change event is fired.
     * On the first call this method initializes the collection.
     * This is required, so no inconsistent state emerges, when a 
     * collection is used as intermediary view. Otherwise data is
     * pumped into the collection and during first use, lazyInitialize is called. 
     */
    def += (v: V): Collection[V] =
    {
        element_added (v)
        add_element (v)
        this
    }

    /**
     * Internal implementation of the add method
     */
    protected def add_element(v: V)

    /**
     * Remove a data tuple from this relation
     * The element is removed and then a change event is fired.
     * On the first call this method initializes the collection.
     * This is required, so no inconsistent state emerges, when a 
     * collection is used as intermediary view. Otherwise data is
     * pumped into the collection and during first use, lazyInitialize is called. 
     */
    def -= (v: V): Collection[V] =
    {
        element_removed (v)
        remove_element (v)
        this
    }

    /**
     * internal implementation remove method
     */
    protected def remove_element(v: V)

}