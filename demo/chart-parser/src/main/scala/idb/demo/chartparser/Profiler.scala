package idb.demo.chartparser

/**
 * @author Mirko Köhler
 */
object Profiler {
 	def main(args : Array[String]): Unit = {
		val words = List("green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green", "green",  "colorless", "ideas", "sleep", "furiously").zipWithIndex

		val parser = SentenceParser

		val iterations = 60
		val warmup = iterations / 5
		val measure = iterations - warmup

		var timeNormalAdd : Long = 0
		var timeNormalRemove : Long = 0

		println("Number of words = " + words.length)
		println("Start Single Add")
		for (i <- 0 to iterations) {
			if (i % (iterations / 20) == 0) println("i = " + i)  else print(".")
			val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
			memoryMXBean.gc()

			val startTime = System.currentTimeMillis()
			words.foreach(parser.input.add)
			val addEndTime = System.currentTimeMillis()
			words.foreach(parser.input.remove)
			val removeEndTime = System.currentTimeMillis()

			if (i >= warmup) {
				timeNormalAdd = timeNormalAdd + (addEndTime - startTime)
				timeNormalRemove = timeNormalRemove + (removeEndTime - addEndTime)
			}
		}


/*		var timeBatchAdd : Long = 0
		var timeBatchRemove : Long = 0
		println("Start Batch Add")
		for (i <- 0 to iterations) {
			if (i % (iterations / 20) == 0) println("i = " + i) else print(".")
			val memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean
			memoryMXBean.gc()

			val startTime = System.currentTimeMillis()
			parser.input.addAll(words)
			val addEndTime = System.currentTimeMillis()
			parser.input.removeAll(words)
			val removeEndTime = System.currentTimeMillis()

			if (i >= warmup) {
				timeBatchAdd = timeBatchAdd + (addEndTime - startTime)
				timeBatchRemove = timeBatchRemove + (removeEndTime - addEndTime)
			}
		}                     */
		println("Finished.")

		val s = "Single Add\n\tAverage time for additions = " + (timeNormalAdd.asInstanceOf[Double] / measure) +
			"ms\n\tAverage time for deletions = " + (timeNormalRemove.asInstanceOf[Double] / measure)/* +
			"ms\nBatch Add\n\tAverage time for additions = " + (timeBatchAdd.asInstanceOf[Double] / measure) +
			"ms\n\tAverage time for deletions = " + (timeBatchRemove.asInstanceOf[Double] / measure) + "ms"  */

		println(s)



	}
}
