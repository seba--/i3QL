package sae
package collections

/**
 * A relation backed by a multi set for efficient access to elements.
 * Each element may have multiple occurrences in this relation.
 */
trait Bag[V]
    extends BagRelation[V]
    with Collection[V]
    with QueryResult[V]
{

    import com.google.common.collect.HashMultiset

    private val data: HashMultiset[V] = HashMultiset.create[V]()

    def foreach[U](f: V => U)
    {
        val it: java.util.Iterator[V] = data.iterator ()
        while (it.hasNext ()) {
            f (it.next ())
        }
    }

    def contains(v: V) = data.contains (v)

    def isDefinedAt(v: V) = {
        data.contains (v)
    }

    def elementCountAt[T >: V](v: T) = {
        data.count (v)
    }

    def size: Int =
    {
        data.size ()
    }

    def singletonValue: Option[V] =
    {
        if (size != 1)
            None
        else
            Some (data.iterator ().next ())
    }

    def add_element(v: V)
    {
        data.add (v)
    }

    def remove_element(v: V)
    {
        data.remove (v)
    }


    def update(oldV: V, newV: V)
    {
        val count = data.count (oldV)
        data.setCount (oldV, 0)
        data.add (newV, count)
        element_updated (oldV, newV)
    }


}