package sae.interpreter.utils

import idb.observer.Observer
import com.google.common.collect.ArrayListMultimap

/**
 * @author Mirko Köhler
 */
class MaterializedMap[Key, Context, Value] extends Observer[(Key, Context, Value)] with PartialFunction[(Key, Context), Value] with Iterable[((Key, Context), Value)] {

	private val materializedMap : ArrayListMultimap[(Key, Context),Value] = ArrayListMultimap.create[(Key, Context),Value]

	override def added(v: (Key, Context, Value)) {
		val key = v._1
		val c = v._2
		val value = v._3
		if (! (materializedMap.containsKey((key, c)) && materializedMap.get((key,c)).contains(value)))
			materializedMap.put((key, v._2), v._3)
	}

	override def removed(v: (Key, Context, Value)) {
		materializedMap.remove((v._1, v._2), v._3)/* match {
			case None => throw new IllegalStateException("Value not contained in map: " + v._2)
			case Some(e) if e != v._2 => throw new IllegalStateException("There is another value " + e + " than the removed value " + v._2)
			case _ => {}
		}   */
	}

	override def updated(oldV: (Key, Context, Value), newV: (Key, Context, Value)): Unit = {
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

	override def apply(t : (Key, Context)) : Value = apply(t._1, t._2)

	def apply(k: Key, c : Context): Value = {
		val result = materializedMap.get((k,c))
		if (result.size != 1)
			throw new IllegalStateException("There are more values at this position. Key = " + k + ", Context = " + c + ", Value = " + result)
		result.get(0)
	}

	override def isDefinedAt(t : (Key, Context)): Boolean = materializedMap.containsKey(t)

	override def iterator: Iterator[((Key, Context), Value)] = new Iterator[((Key, Context), Value)](){
		val it = materializedMap.entries().iterator()

		override def hasNext: Boolean = it.hasNext

		override def next(): ((Key, Context), Value) = {
			val e = it.next()
			(e.getKey, e.getValue)
		}
	}


}
