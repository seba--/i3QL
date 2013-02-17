package sae.collections

import sae._
import deltas.{Update, Deletion, Addition}


/**
 * An index backed by a guava ListMultimap.
 * The index may have multiple values for a single key.
 * The index stores multiple equal key-value pairs.
 * Thus this Index is suited for bag semantics.
 *
 * Multi-value semantics is a pre-requisite for the index to work with
 * arbitrary data. Parts of a tuple may be defined as index and must not be
 * unique in any way.
 */
class TransactionalBagIndex[K, V](val relation: Relation[V],
                                  val keyFunction: V => K)
                                 (val traceDeletions: Boolean = false)
    extends Index[K, V]
{


    private var map = com.google.common.collect.ArrayListMultimap.create[K, V]()

    def size = map.size ()

    def clear() {
        map = com.google.common.collect.ArrayListMultimap.create[K, V]()
    }

    def foreachKey[U](f: (K) => U) {
        val it = map.keySet ().iterator ()
        while (it.hasNext) {
            val next = it.next ()
            f (next)
        }
    }

    def put(key: K, value: V) {
        map.put (key, value)
    }

    def get(key: K): Option[Traversable[V]] = {
        val l = map.get (key)
        if (l.isEmpty)
            return None
        Some (new ValueListTraverser (l))
    }

    private class ValueListTraverser[V](val values: java.util.List[V]) extends Traversable[V]
    {
        def foreach[T](f: V => T) {
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

    def foreach[U](f: ((K, V)) => U) {
        val it: java.util.Iterator[java.util.Map.Entry[K, V]] = map.entries ().iterator
        while (it.hasNext) {
            val next = it.next ()
            f ((next.getKey, next.getValue))
        }
    }


    override def updated(oldV: V, newV: V) {
        throw new UnsupportedOperationException
    }

    override def removed(v: V) {
        if (traceDeletions) {
            val k = keyFunction (v)
            remove_element (k, v)
            element_removed ((k, v))
        }
    }

    override def added(v: V) {
        if (!traceDeletions) {
            val k = keyFunction (v)
            add_element (k, v)
            element_added ((k, v))
        }
    }


    def add_element(key: K, value: V) {
        map.put (key, value)
    }


    def remove_element(key: K, value: V) {
        map.put (key, value)
    }

    def update_element(oldKey: K, oldV: V, newKey: K, newV: V) {
        throw new UnsupportedOperationException
    }

    def updated[U <: V](update: Update[U]) {
        throw new UnsupportedOperationException
    }

    def added[U <: V](addition: Addition[U]) {
        throw new UnsupportedOperationException
    }

    def deleted[U <: V](deletion: Deletion[U]) {
        throw new UnsupportedOperationException
    }

    def modified[U <: V](additions: scala.collection.immutable.Set[Addition[U]], deletions: scala.collection.immutable.Set[Deletion[U]], updates: scala.collection.immutable.Set[Update[U]]) {
        throw new UnsupportedOperationException
    }

}
