package idb.collections.impl

import java.io.PrintStream

import idb.{MaterializedView, Relation}
import idb.observer.{NotifyObservers, Observer}

import scala.collection.generic.{Growable, Shrinkable}
import scala.collection.{LinearSeq, mutable}

/**
  * A materialized bag that stores its elements ordered.
  *
  * @author mirko
  */
class SortedBag[T, Key](relation : Relation[T], key : T => Key)(implicit ordering: Ordering[Key]) extends MaterializedView[T] with Observer[T] with NotifyObservers[T] {
	relation.addObserver(this)

	private val data = new SortedKeyList[T, Key](key)


	override def added(v: T): Unit = {
		data += v
		notify_added(v)
	}

	override def addedAll(vs: Seq[T]): Unit = {
		vs.foreach(v => data += v)
		notify_addedAll(vs)
	}


	override def updated(oldV: T, newV: T): Unit = {
		data.remove(oldV)
		data.add(newV)
		notify_updated(oldV, newV)
	}

	override def removed(v: T): Unit = {
		data -= v
		notify_removed(v)
	}

	override def removedAll(vs: Seq[T]): Unit = {
		vs.foreach(v => data -= v)
		notify_removedAll(vs)
	}


	override def contains[U >: T](element: U): Boolean =
		//TODO: This does not use the binary search.
		data.contains(element)

	/**
	  * Returns the count for a given element.
	  * In case an add/remove/update event is in progression, this always returns the value before the event.
	  */
	override def count[U >: T](element: U): Int =
		//TODO: Implement it faster (ex: binary search)
		data.count(e => e == element)

	override def foreachWithCount[B](f: (T, Int) => B): Unit = {
		var i = 0
		data.foreach(e => {
			f(e, i)
			i = i + 1
		})
	}

	/**
	  * Returns the size of the view in terms of elements.
	  * This can be a costly operation.
	  * Implementors should cache the value in a self-maintained view, but clients can not rely on this.
	  */
	override def size: Int =
		data.size

	/**
	  * Runtime information whether a compiled query is a set or a bag
	  */
	override def isSet: Boolean =
		false

	override def foreach[B](f: (T) => B): Unit =
		data.foreach(f)

	override def children: Seq[Relation[_]] =
		Seq(relation)

	override protected[idb] def resetInternal(): Unit =
		data.clear()

	override protected[idb] def printInternal(out: PrintStream)(implicit prefix: String): Unit = {
		out.println(prefix + s"SortedBag(")
		printNested(out, relation)
		out.println(prefix + ")")
	}

	override def toString() : String = {
		s"SortedBag($relation, key = $key, data = $data)"
	}


}

class SortedKeyList[T, Key](key : T => Key)(implicit ordering: Ordering[Key]) extends LinearSeq[T] with Growable[T] with Shrinkable[T] {
	import java.util.ArrayList

	private val list : ArrayList[(Key, T)] = new ArrayList[(Key, T)]()

	override def apply(idx: Int): T = list.get(idx)._2

	override def length: Int = list.size()

	def add(element : T): Unit = {
		val k = key(element)
		val index = findIndex(k)
		list.add(index, (k, element))
	}

	def remove(element : T): Unit = {
		val k = key(element)
		val index = findIndex(k)
		list.remove(index)
	}

	override def isEmpty: Boolean =
		list.isEmpty

	override def iterator: Iterator[T] =
		new Iterator[T]() {
			val it = list.iterator()
			override def hasNext: Boolean = it.hasNext
			override def next(): T = it.next()._2
		}

//	def contains(elem: T): Boolean = {
//		val k = key(elem)
//		val index = findIndex(k)
//
//		data.get(index)._2 == elem
//	}

	/**
	  * Uses binary search to find the index for an element. This may be the index of the element
	  * if it is already present in the list or the index where it should be inserted.
	  */
	private def findIndex(k : Key) : Int = {
		var l = 0
		var r = list.size - 1

		while (l <= r) {
			var m = (l + r) / 2
			ordering.compare(list.get(m)._1, k) match {
				case -1 => l = m + 1
				case 1 => r = m - 1
				case 0 => return m
			}
		}

		return l
	}

	override def toString() : String = {
		val sb = new StringBuilder()
		sb.append("[")
		var isFirst = true
		foreach[Unit](e => {
			if (!isFirst) sb.append(", ")
			isFirst = false
			sb.append(e)
		})
		sb.append("]")
		sb.toString()
	}


	override def +=(elem: T): SortedKeyList.this.type ={
		add(elem)
		this
	}

	override def -=(elem: T): SortedKeyList.this.type = {
		remove(elem)
		this
	}

	override def clear(): Unit = {
		list.clear()
	}
}
