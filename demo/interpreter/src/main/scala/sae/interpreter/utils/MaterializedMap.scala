package sae.interpreter.utils

import idb.Relation
import idb.observer.{NotifyObservers, Observer}
import com.google.common.collect.ArrayListMultimap

/**
 * @author Mirko Köhler
 */
class MaterializedMap[Key, Value] extends Observer[(Key, Value)] with PartialFunction[Key, Value] with Iterable[(Key, Value)] {
	import com.google.common.collect.ArrayListMultimap

	private val materializedMap : ArrayListMultimap[Key, Value] = ArrayListMultimap.create[Key, Value]

	override def added(v: (Key, Value)) {
		val key = v._1
		val value = v._2
		if (! (materializedMap.containsKey(key) && materializedMap.get(key).contains(value)))
			materializedMap.put(key, value)
	}

  override def addedAll(vs: Seq[(Key, Value)]) {
    for (v <- vs)
      added(v)
  }

	override def removed(v: (Key, Value)) {
		materializedMap.remove(v._1, v._2)/* match {
			case None => throw new IllegalStateException("Value not contained in map: " + v._2)
			case Some(e) if e != v._2 => throw new IllegalStateException("There is another value " + e + " than the removed value " + v._2)
			case _ => {}
		}   */
	}

  def removedAll(vs: Seq[(Key, Value)]): Unit = {
    for (v <- vs)
      removed(v)
  }

	override def updated(oldV: (Key, Value), newV: (Key, Value)): Unit = {
		if (oldV._1 != newV._1)
			throw new IllegalArgumentException("oldKey != newKey")
		removed(oldV)
		added(newV)/* match {
			case None => throw new IllegalStateException("Value not contained in map: " + oldV._2)
			case Some(e) if e != oldV._2 => throw new IllegalStateException("There is another value " + e + " than the updated value " + oldV._2)
			case _ => {}
		} */
	}


	override def endTransaction() { }



	def apply(k: Key): Value = {
		val result = materializedMap.get(k)
		if (result.size > 1)
			throw new IllegalStateException("There are more values at this position. Key = " + k + ", Value = " + result)
		else if (result.size == 0)
			throw new IllegalStateException("There are no values at this position. Key = " + k)
		result.get(0)
	}

	override def isDefinedAt(t : Key): Boolean = materializedMap.containsKey(t)

	override def iterator: Iterator[(Key,  Value)] = new Iterator[(Key, Value)](){
		val it = materializedMap.entries().iterator()

		override def hasNext: Boolean = it.hasNext

		override def next(): (Key, Value) = {
			val e = it.next()
			(e.getKey, e.getValue)
		}
	}

}
