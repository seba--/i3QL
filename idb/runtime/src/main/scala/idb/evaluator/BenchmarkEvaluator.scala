package idb.evaluator

import idb.Relation
import idb.observer.Observer

class BenchmarkEvaluator[T](
	val relation : Relation[T],
	val sendTimeOf : T => Long
) extends Observer[T] {
	relation.addObserver(this)

	var countElements : Long = 0
	var countEvents : Long = 0

	var firstEvent : Long = -1

	var eventTimes : List[(Long, Long)] = Nil

	private def handleEvent(v : T): Unit = {
		if (firstEvent == -1)
			firstEvent = System.currentTimeMillis()

		println("Benchmark.handle#" + v)

		countEvents = countEvents + 1
		eventTimes = (sendTimeOf(v), System.currentTimeMillis()) :: eventTimes
	}

	def getSummary(time : Long) = {


		val averageSendingDelay : Double = eventTimes.foldLeft(0.0)((res, t) =>
			res + ((t._2 - t._1	).toDouble / countEvents)
		)

		val totalDelay : Long = time - firstEvent
		(countEvents, eventTimes.size, totalDelay, averageSendingDelay)
	}


	override def updated(oldV: T, newV: T): Unit = {
		handleEvent(newV)
	}

	override def removed(v: T): Unit = {
		countElements = countElements - 1
		handleEvent(v)
	}

	override def removedAll(vs: Seq[T]): Unit = {
		countElements = countElements - vs.size
		vs foreach handleEvent
	}

	override def added(v: T): Unit = {
		countElements = countElements + 1
		handleEvent(v)
	}

	override def addedAll(vs: Seq[T]): Unit = {
		countElements = countElements + vs.size
		vs foreach handleEvent
	}

	override def endTransaction(): Unit = ???
}
