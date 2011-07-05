package sae
package collections

/**
 * An index backed by a guave HashMultimap.
 * The index may have multiple values for a single key.
 * The index does not store multiple equal key-value pairs.
 * Thus this Index is suited for set semantics.
 *
 * Multi-value semantics is a pre-requisite for the index to work with
 * arbitrary data. Parts of a tuple may be defined as index and must not be
 * unique in any way.
 */
class HashSetIndex[K <: AnyRef, V <: AnyRef](
    val relation: MaterializedView[V],
    val keyFunction: V => K
)
        extends Collection[(K, V)]
                with Index[K, V]
{

    private val map = com.google.common.collect.HashMultimap.create[K, V]()

    protected def put_internal(key: K, value: V)
    {
        map.put(key, value)
    }

    protected def get_internal(key: K): Option[Traversable[V]] =
    {
        val l = map.get(key)
        if (l.isEmpty)
            None
        Some(new ValueListTraverser(l))
    }

    private class ValueListTraverser[V](val values: java.util.Set[V]) extends Traversable[V]
    {
        def foreach[T](f: V => T)
        {
            val it: java.util.Iterator[V] = values.iterator
            while (it.hasNext) {
                val next = it.next()
                f(next)
            }
        }
    }

    protected def isDefinedAt_internal(key: K): Boolean = map.containsKey(key)


    protected def elementCountAt_internal(key: K) =
        if( !map.containsKey(key) )
        {
            0
        }
        else
        {
            map.get(key).size()
        }

    def materialized_foreach[U](f: ((K, V)) => U)
    {
        val it: java.util.Iterator[java.util.Map.Entry[K, V]] = map.entries().iterator
        while (it.hasNext) {
            val next = it.next()
            f((next.getKey, next.getValue))
        }
    }

    def materialized_size: Int =
        map.size

    def materialized_singletonValue: Option[(K, V)] =
    {
        if (size != 1)
            None
        else {
            val next = map.entries().iterator().next()
            Some((next.getKey, next.getValue))
        }
    }

    protected def materialized_contains(v: (K, V)) =
        map.containsEntry(v._1, v._2)

    def add_element(kv: (K, V))
    {
        map.put(kv._1, kv._2)
    }

    def remove_element(kv: (K, V))
    {
        map.remove(kv._1, kv._2)
    }

    def update_element(key: K, oldV: V, newV: V)
    {
        val valueSet = map.get(key)
        valueSet.remove(oldV)
        valueSet.add(newV)
    }

}
