package idb.benchmark

import idb.Relation

import scala.collection.mutable

/**
  * Evaluates the throughput of a relation by resulting total receiving delay and events per second.
  */
class ThroughputEvaluator[Domain](
	val relation : Relation[Domain],
	var countEvaluator : CountEvaluator[Domain] = null
) extends Evaluator[Domain, (Double, Long)] {
	relation.addObserver(this)
	if (countEvaluator == null)
		countEvaluator = new CountEvaluator[Domain](relation)

	var firstEventTime : Long = -1
	var lastEventTime : Long = -1

	private def handleEvent(): Unit = {
		val currTime = System.currentTimeMillis()
		if (firstEventTime == -1)
			firstEventTime = currTime

		lastEventTime = currTime
	}

	override def result(): (Double, Long) = {
		val time = lastEventTime - firstEventTime
		val timeInSec : Double = time.toDouble / 1000
		val eventsPerSec : Double = countEvaluator.countEvents.toDouble / timeInSec

		(eventsPerSec, time)
	}

	override def updated(oldV: Domain, newV: Domain): Unit = {
		handleEvent()
	}

	override def removed(v: Domain): Unit = {
		handleEvent()
	}

	override def removedAll(vs: Seq[Domain]): Unit = {
		handleEvent()
	}

	override def added(v: Domain): Unit = {
		handleEvent()
	}

	override def addedAll(vs: Seq[Domain]): Unit = {
		handleEvent()
	}


}
