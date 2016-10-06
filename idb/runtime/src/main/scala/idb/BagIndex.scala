package idb

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

	private val map = com.google.common.collect.ArrayListMultimap.create[K, V]()

	lazyInitialize ()

	def size = map.size ()

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

	def contains(key: K): Boolean = map.containsKey (key)


	def count(key: K) =
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

	def add_element(key: K, value: V) {
		map.put (key, value)
	}


	def remove_element(key: K, value: V) {
		map.remove (key, value)
	}

	def update_element(oldKey: K, oldV: V, newKey: K, newV: V) {
		val list = map.get (oldKey)
		val it = list.iterator ()
		val retainedMap = new java.util.LinkedList[V]()
		val newMap = new java.util.LinkedList[V]()
		while (it.hasNext) {
			val next = it.next ()
			if (next == oldV)
				newMap.add (newV)
			else
				retainedMap.add (next)
		}
		map.replaceValues (oldKey, retainedMap)
		map.putAll (newKey, newMap)
	}

	override protected def resetInternal(): Unit = {
		map.clear()
	}
}
