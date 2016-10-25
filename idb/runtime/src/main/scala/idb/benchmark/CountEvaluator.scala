package idb.benchmark

import idb.Relation
import idb.observer.Observer



/**
  * Counts the number of events that have been received. Batches count as multiple events.
  */
class CountEvaluator[Domain](
	val relation : Relation[Domain]
) extends Evaluator[Domain, Int] {
	relation.addObserver(this)

	var count = 0

	override def result(): Int =
		count

	override def updated(oldV: Domain, newV: Domain): Unit = {
		count = count + 1
	}

	override def removed(v: Domain): Unit = {
		count = count + 1
	}

	override def removedAll(vs: Seq[Domain]): Unit = {
		count = count + vs.size
	}

	override def added(v: Domain): Unit = {
		count = count + 1
	}

	override def addedAll(vs: Seq[Domain]): Unit = {
		count = count + vs.size
	}


}
