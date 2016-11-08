package idb.benchmark

import idb.Relation
import idb.observer.Observer



/**
  * Counts the number of events that have been received. Batches count as multiple events.
  */
class CountEvaluator[Domain](
	val relation : Relation[Domain]
) extends Evaluator[Domain, (Int, Int)] {
	relation.addObserver(this)

	var countEvents = 0
	var countEntries = 0

	override def result(): (Int, Int) =
		(countEvents, countEntries)

	override def updated(oldV: Domain, newV: Domain): Unit = {
		countEvents = countEvents + 1
	}

	override def removed(v: Domain): Unit = {
		countEvents = countEvents + 1
		countEntries = countEntries - 1
	}

	override def removedAll(vs: Seq[Domain]): Unit = {
		countEvents = countEvents + vs.size
		countEntries = countEntries - vs.size
	}

	override def added(v: Domain): Unit = {
		countEvents = countEvents + 1
		countEntries = countEntries + 1
	}

	override def addedAll(vs: Seq[Domain]): Unit = {
		countEvents = countEvents + vs.size
		countEntries = countEntries + vs.size
	}


}
