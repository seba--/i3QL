package sae.interpreter.utils

import idb.observer.Observer
import com.google.common.collect.ArrayListMultimap

/**
 * @author Mirko Köhler
 */
class MaterializedMap[Key, Value] extends Observer[(Key, Value)] with PartialFunction[Key, Value] with Iterable[(Key, Value)] {

	private val materializedMap : ArrayListMultimap[Key,Value] = ArrayListMultimap.create[Key,Value]

	override def added(v: (Key, Value)) {
		materializedMap.put(v._1, v._2)
	}

	override def removed(v: (Key, Value)) {
		materializedMap.remove(v._1, v._2)/* match {
			case None => throw new IllegalStateException("Value not contained in map: " + v._2)
			case Some(e) if e != v._2 => throw new IllegalStateException("There is another value " + e + " than the removed value " + v._2)
			case _ => {}
		}   */
	}

	override def updated(oldV: (Key, Value), newV: (Key, Value)): Unit = {
		if (oldV._1 != newV._1)
			throw new IllegalArgumentException("oldKey != newKey")

		materializedMap.put(newV._1, newV._2)/* match {
			case None => throw new IllegalStateException("Value not contained in map: " + oldV._2)
			case Some(e) if e != oldV._2 => throw new IllegalStateException("There is another value " + e + " than the updated value " + oldV._2)
			case _ => {}
		} */
	}


	override def endTransaction() { }

	override def apply(v1: Key): Value = {
		val result = materializedMap.get(v1)
		if (result.size != 1)
			throw new IllegalStateException("There are more values at this position.")
		result.get(0)
	}

	override def isDefinedAt(x: Key): Boolean = materializedMap.containsKey(x)

	override def iterator: Iterator[(Key, Value)] = new Iterator[(Key, Value)](){
		val it = materializedMap.entries().iterator()

		override def hasNext: Boolean = it.hasNext

		override def next(): (Key, Value) = {
			val e = it.next()
			(e.getKey, e.getValue)
		}
	}


}
