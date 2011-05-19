package sae
package collections

/**
 * An index backed by a guave HashMultimap.
 * Multi-value semantics is a pre-requisite for the index to work with
 * arbitrary data. Parts of a tuple may be defined as index and must not be
 * unique in any way.
 */
class HashMultiMap[K <: AnyRef, V <: AnyRef](
    val relation : MaterializedView[V],
    val keyFunction : V => K)
        extends Collection[(K, V)]
        with Index[K, V] {

    private val map = com.google.common.collect.HashMultimap.create[K, V]()

    protected def put_internal(key : K, value : V) : Unit =
        map.put(key, value)

    protected def get_internal(key : K) : Option[Traversable[V]] =
        {
            val l = map.get(key)
            if (l.isEmpty)
                None
            Some(new ValueListTraverser(l))
        }

    private class ValueListTraverser[V](val values :java.util.Set[V]) extends Traversable[V] {
        def foreach[T](f : V => T) : Unit = {
            val it : java.util.Iterator[V] = values.iterator
            while (it.hasNext()) {
                val next = it.next()
                f(next)
            }
        }
    }

    protected def isDefinedAt_internal(key : K) : Boolean =
        !map.get(key).isEmpty

    def materialized_foreach[U](f : ((K, V)) => U) : Unit =
        {
            val it : java.util.Iterator[java.util.Map.Entry[K, V]] = map.entries().iterator
            while (it.hasNext()) {
                val next = it.next()
                f((next.getKey, next.getValue))
            }
        }

    def materialized_size : Int =
        map.size

    def materialized_singletonValue : Option[(K, V)] =
        {
            if (size != 1)
                None
            else {
                val next = map.entries().iterator().next()
                Some((next.getKey, next.getValue))
            }
        }

    def add_element(kv : (K, V)) : Unit =
        {
            map.put(kv._1, kv._2)
        }

    def remove_element(kv : (K, V)) : Unit =
        {
            map.remove(kv._1, kv._2)
        }

    def update_element(key : K, oldV : V, newV : V) : Unit =
        {
            val valueSet = map.get(key)
            valueSet.remove(oldV)
            valueSet.add(newV)
        }
}
