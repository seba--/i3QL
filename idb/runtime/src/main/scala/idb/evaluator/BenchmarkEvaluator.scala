package idb.evaluator

import idb.Relation
import idb.observer.Observer

class BenchmarkEvaluator[T](
	val relation : Relation[T],
	val sendTimeOf : T => Long,
	val expectedNumberOfResults : Long,
	val warmup : Long = 0
) extends Observer[T] {
	require(warmup < expectedNumberOfResults)
	relation.addObserver(this)

	var countElements : Long = 0
	var countEvents : Long = 0

	var firstEvent : Long = -1
	var lastEvent : Long = -1

	var eventTimes : List[(Long, Long)] = Nil

	private def handleEvent(v : T): Unit = {
		val currTime = System.currentTimeMillis()
		countEvents = countEvents + 1

		if (countEvents > warmup) {
			println(s"Benchmark.handle($currTime, $countEvents)#$v")

			if (firstEvent == -1)
				firstEvent = currTime

			eventTimes = (sendTimeOf(v), currTime) :: eventTimes

			if (countEvents == expectedNumberOfResults)
				lastEvent = currTime
		} else {
			println(s"Benchmark.warmup($currTime, $countEvents)...")
		}
	}

	def getSummary = {
		require(lastEvent != -1)

		//Do not use countEvents because it also includes warmup
		val events = eventTimes.size

		val averageSendingDelay : Double = eventTimes.foldLeft(0.0)((res, t) =>
			res + ((t._2 - t._1	).toDouble / events)
		)

		val totalDelay : Long = lastEvent - firstEvent

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


}
