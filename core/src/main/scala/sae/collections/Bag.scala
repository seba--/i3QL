package sae
package collections

/**
 * A relation backed by a multi set for efficient access to elements.
 * Each element may have multiple occurrences in this relation.
 */
trait Bag[V]
    extends BagRelation[V]
    with Collection[V]
    with LazyInitializedQueryResult[V]
{

    import com.google.common.collect.HashMultiset
    private val data: HashMultiset[V] = HashMultiset.create[V]()

    def materialized_size: Int =
    {
        data.size ()
    }

    def materialized_singletonValue: Option[V] =
    {
        if (size != 1)
            None
        else
            Some (data.iterator ().next ())
    }

    def add_element(v: V): Unit =
    {
        data.add (v)
    }

    def remove_element(v: V): Unit =
    {
        data.remove (v)
    }

    def materialized_foreach[U](f: V => U): Unit =
    {
        val it: java.util.Iterator[V] = data.iterator ()
        while (it.hasNext ()) {
            f (it.next ())
        }
    }

    protected def materialized_contains(v: V) = data.contains (v)

    def update(oldV: V, newV: V)
    {
        val count = data.count (oldV)
        data.setCount (oldV, 0)
        data.add (newV, count)
        element_updated (oldV, newV)
    }


    protected def isDefinedAt_internal(v: V) = {
        data.contains(v)
    }

    protected def elementCountAt_internal(v: V) = {
        data.count(v)
    }
}