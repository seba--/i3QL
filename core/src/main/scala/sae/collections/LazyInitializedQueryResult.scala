package sae.collections

import sae.QueryResult
import sae.capabilities.LazyInitializedRelation

/**
 * This view materializes its elements and thus requires
 * some form of intermediate storage.
 */
trait LazyInitializedQueryResult[V]
    extends QueryResult[V]
    with LazyInitializedRelation[V]
{


    /**
     * We give an abstract implementation of foreach, with
     * lazy initialization semantics.
     * But clients are required to implement their own
     * foreach method, with concrete semantics.
     */
    def foreach[T](f: (V) => T)
    {
        if (!isInitialized) {
            lazyInitialize ()
            setInitialized()
        }
        materialized_foreach (f)
    }

    /**
     * The internal implementation that iterates only over materialized
     * data.
     */
    protected def materialized_foreach[T](f: (V) => T)

    def size: Int = {
        if (!isInitialized) {
            lazyInitialize ()
            setInitialized()
        }
        materialized_size
    }

    /**
     * The internal implementation that yields the size
     */
    protected def materialized_size: Int

    def singletonValue: Option[V] = {
        if (!isInitialized) {
            lazyInitialize ()
            setInitialized()
        }
        materialized_singletonValue
    }

    /**
     * The internal implementation that yields the singletonValue
     */
    protected def materialized_singletonValue: Option[V]

    def contains(v: V): Boolean = {
        if (!isInitialized) {
            lazyInitialize ()
            setInitialized()
        }
        materialized_contains (v)
    }

    /**
     * The internal implementation that yields the singletonValue
     */
    protected def materialized_contains(v: V): Boolean


}