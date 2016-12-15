package idb

import com.google.common.collect.{ArrayListMultimap, Multimaps}

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
class BagIndex[K, V](val relation: Relation[V],
					 val keyFunction: V => K)
	extends Index[K, V] {

	relation addObserver this

	val isSet : Boolean = false
	private val coll = Multimaps.synchronizedListMultimap[K, V](ArrayListMultimap.create())

	def size = coll.size ()

	def foreachKey[U](f: (K) => U) {
		coll.synchronized {
			val it = coll.keySet ().iterator ()
			while (it.hasNext) {
				val next = it.next ()
				f (next)
			}
		}
	}

	def put(key: K, value: V) {
		coll.put (key, value)
	}

	def get(key: K): Option[Traversable[V]] = {
		val l = coll.get (key)
		if (l.isEmpty)
			return None
		Some (new ValueListTraverser (l))
	}

	private class ValueListTraverser[V](val values: java.util.Collection[V]) extends Traversable[V]	{
		def foreach[T](f: V => T) {
			coll.synchronized {
				val it: java.util.Iterator[V] = values.iterator
				while (it.hasNext) {
					val next = it.next ()
					f (next)
				}
			}

		}
	}

	def contains(key: K): Boolean =
		coll.containsKey (key)


	def count(key: K) =  {
		if (coll.containsKey (key))
			coll.get(key).size
		else
			0
	}



	def foreach[U](f: ((K, V)) => U) {
		coll.synchronized {
			val it: java.util.Iterator[java.util.Map.Entry[K, V]] = coll.entries().iterator
			while (it.hasNext) {
				val next = it.next ()
				f ((next.getKey, next.getValue))
			}
		}
	}

	def add_element(key: K, value: V) {
		coll.put (key, value)
	}

	def remove_element(key: K, value: V) {
		coll.remove (key, value)
	}

	def update_element(oldKey: K, oldV: V, newKey: K, newV: V) {
		val list = coll.get (oldKey)

		val retainedMap = new java.util.LinkedList[V]()
		val newMap = new java.util.LinkedList[V]()

		coll.synchronized {
			val it = list.iterator ()
			while (it.hasNext) {
				val next = it.next ()
				if (next == oldV)
					newMap.add (newV)
				else
					retainedMap.add (next)
			}
		}

		coll.replaceValues (oldKey, retainedMap)
		coll.putAll (newKey, newMap)
	}

	override protected[idb] def resetInternal(): Unit = {
		coll.clear()
	}
}
