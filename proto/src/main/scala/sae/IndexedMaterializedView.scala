package sae

/**
 * This trait represents a materialized view that can be indexed
 * for faster joins, etc.
 */
trait IndexedMaterializedView[V <: AnyRef]
        extends MaterializedView[V] {

    /**
     * returns an index for specified key function
     */
    def index[K <: AnyRef](keyFunction : V => K) : Index[K, V]

}