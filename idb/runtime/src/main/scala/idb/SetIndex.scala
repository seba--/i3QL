package idb

import com.google.common.collect.{ArrayListMultimap, Multimaps}

class SetIndex[K, V](val relation: Relation[V],
					 val keyFunction: V => K)
	extends Index[K, V]
{

	relation addObserver this

	val isSet = true
	private val coll = Multimaps.synchronizedListMultimap(ArrayListMultimap.create[K, V]())


	def foreachKey[U](f: (K) => U) {
		coll.synchronized {
			val it = coll.keys ().iterator ()
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

	private class ValueListTraverser[V](val values: java.util.Collection[V]) extends Traversable[V] {

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

	def contains(key: K): Boolean = coll.containsKey (key)


	def count(key: K) = {
		if (coll.containsKey (key))
			coll.get (key).size ()
		else
			0
	}



	override def foreach[U](f: ((K, V)) => U) {
		coll.synchronized {
			val it: java.util.Iterator[java.util.Map.Entry[K, V]] = coll.entries ().iterator
			while (it.hasNext) {
				val next = it.next ()
				f ((next.getKey, next.getValue))
			}
		}
	}


	override def size = coll.values ().size ()

	def add_element(k: K, v: V) {
		coll.put (k, v)
	}

	def remove_element(k: K, v: V) {
		coll.remove (k, v)
	}

	def update_element(oldKey: K, oldV: V, newKey: K, newV: V) {
		remove_element (oldKey, oldV)
		add_element (newKey, newV)
	}

	override protected def resetInternal(): Unit = {
		coll.clear()
	}

}
