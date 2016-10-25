package idb.benchmark

import idb.Relation
import scala.collection.mutable
/**
  * Created by mirko on 25.10.16.
  */
class DelayEvaluator[Domain](
	val relation : Relation[Domain],
	val sendTimeOf : Domain => Long,
	val expectedBufferLength : Int = 0
) extends Evaluator[Domain, Double] {
	relation.addObserver(this)

	val eventTimes : mutable.Buffer[(Long, Long)] = mutable.Buffer.fill[(Long, Long)](expectedBufferLength)((0,0))
	eventTimes.clear()

	private def handleEvent(v : Domain): Unit = {
		val currTime = System.currentTimeMillis()
		eventTimes.append((sendTimeOf(v), currTime))
		eventTimes.size
	}

	override def result(): Double = {
		val totalDelay : Long = eventTimes.foldLeft(0L)((res, t) =>
			res + (t._2 - t._1)
		)
		val averageDelay = totalDelay.toDouble / eventTimes.size.toDouble

		averageDelay
	}

	override def updated(oldV: Domain, newV: Domain): Unit = {
		handleEvent(newV)
	}

	override def removed(v: Domain): Unit = {
		handleEvent(v)
	}

	override def removedAll(vs: Seq[Domain]): Unit = {
		vs foreach handleEvent
	}

	override def added(v: Domain): Unit = {
		handleEvent(v)
	}

	override def addedAll(vs: Seq[Domain]): Unit = {
		vs foreach handleEvent
	}


}
