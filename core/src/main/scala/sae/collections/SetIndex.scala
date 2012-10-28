package sae
package collections

/**
 * An index backed by a guava HashMultimap.
 * The index may have multiple values for a single key.
 * The index does not store multiple equal key-value pairs.
 * Thus this Index is suited for set semantics.
 *
 * Multi-value semantics is a pre-requisite for the index to work with
 * arbitrary data. Parts of a tuple may be defined as index and must not be
 * unique in any way.
 */
class SetIndex[K, V](val relation: Relation[V],
                     val keyFunction: V => K)
    extends Index[K, V]
{

    private val map = com.google.common.collect.HashMultimap.create[K, V]()

    lazyInitialize()

    def foreachKey[U](f: (K) => U) {
        val it = map.keys ().iterator ()
        while (it.hasNext) {
            val next = it.next ()
            f (next)
        }
    }

    def put(key: K, value: V)
    {
        map.put (key, value)
    }

    def get(key: K): Option[Traversable[V]] =
    {
        val l = map.get (key)
        if (l.isEmpty)
            return None
        Some (new ValueListTraverser (l))
    }

    private class ValueListTraverser[V](val values: java.util.Set[V]) extends Traversable[V]
    {
        def foreach[T](f: V => T)
        {
            val it: java.util.Iterator[V] = values.iterator
            while (it.hasNext) {
                val next = it.next ()
                f (next)
            }
        }
    }

    def isDefinedAt(key: K): Boolean = map.containsKey (key)


    def elementCountAt(key: K) =
        if (map.containsKey (key))
        {
            map.get (key).size ()
        }
        else
        {
            0
        }

    def foreach[U](f: ((K, V)) => U)
    {
        val it: java.util.Iterator[java.util.Map.Entry[K, V]] = map.entries ().iterator
        while (it.hasNext) {
            val next = it.next ()
            f ((next.getKey, next.getValue))
        }
    }


    def size = map.values ().size ()

    def add_element(k: K, v: V)
    {
        map.put (k, v)
    }

    def remove_element(k: K, v: V)
    {
        map.remove (k, v)
    }

    def update_element(oldKey: K, oldV: V, newKey: K, newV: V)
    {
        remove_element(oldKey, oldV)
        add_element(newKey, newV)
    }

}
