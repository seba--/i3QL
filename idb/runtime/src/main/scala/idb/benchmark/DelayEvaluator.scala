package idb.benchmark

import idb.Relation
import scala.collection.mutable

class DelayEvaluator[Domain](
	val relation : Relation[Domain],
	val sendTimeOf : Domain => Long,
	val expectedBufferLength : Int = 16
) extends Evaluator[Domain, (Double, Long)] {
	relation.addObserver(this)

	val eventTimes : mutable.Buffer[(Long, Long)] = mutable.Buffer.fill[(Long, Long)](expectedBufferLength)((0,0))
	eventTimes.clear()

	private def handleEvent(v : Domain): Unit = {
		val currTime = System.currentTimeMillis()
		eventTimes.append((sendTimeOf(v), currTime))
		eventTimes.size
	}

	override def result(): (Double, Long) = {
		(averageDelay, medianDelay)
	}

	def averageDelay : Double = {
		if (eventTimes.isEmpty)
			return -1

		val totalDelay : Long = eventTimes.foldLeft(0L)((res, t) =>
			res + (t._2 - t._1)
		)
		val averageDelay = totalDelay.toDouble / eventTimes.size.toDouble

		averageDelay
	}

	def medianDelay : Long = {
		if (eventTimes.isEmpty)
			return -1

		val delays = eventTimes.map(t =>
			t._2 - t._1
		)

		delays.sorted.apply(delays.size / 2)

	}

	def runtime : Long = {
		if (eventTimes.isEmpty)
			return -1

		val start = eventTimes.head._1
		val end = eventTimes.last._2

		end - start
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
