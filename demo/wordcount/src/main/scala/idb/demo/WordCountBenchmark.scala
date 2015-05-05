package idb.demo

import java.io.{FileWriter, BufferedWriter}
import java.nio.file.{Path, Paths}

import idb.Relation

/**
 * @author Mirko Köhler
 */
class WordCountBenchmark(val factory : WordCountFactory) {

	val directory : Path = Paths.get("res","dta_kernkorpus_2014-03-10")

	val isMaterialized = false
	val cacheFiles = true

	val warmupIterations = 1
	val measureIterations = 1

	def run(): Unit = {

		//First iteration with saving of results
		println("benchmarking " + factory.getClass.getSimpleName)
		println("Initial run...")
		val time = executeWordCount(saveResults = true)
		println(s"time: $time ms")

		println("Warmup...")
		for (i <- 2 to warmupIterations) {
			println(s"Iteration $i...")

			val time = executeWordCount(saveResults = false)
			println(s"time: $time ms")
		}

		println("Measure...")
		var timeList : List[Long] = Nil

		for (i <- 1 to measureIterations) {
			println(s"Iteration $i...")

			val time = executeWordCount(saveResults = false)
			println(s"time: $time ms")
			timeList = time :: timeList
		}

		println(s"Measure finished. Average time: ${timeList.sum / timeList.length}ms")

	}

	private def writeResults(rel : Relation[(String,Int)]): Unit = {
		val fw = new BufferedWriter(new FileWriter(factory.getClass.getSimpleName + "_results.txt"))
		rel.foreach(x => fw.write(s"${x._1};${x._2}\n"))
		fw.close()
	}

	private def executeWordCount(saveResults : Boolean): Long = {
		System.gc()
		Thread.sleep(2500)
		println("Start...")
		println(s"Free/total memory: ${Runtime.getRuntime.freeMemory() / 1000000}mB/${Runtime.getRuntime.totalMemory() / 1000000}mB")

		var wc = factory.create(directory, isMaterialized, cacheFiles)
		wc.initialize()
		val before = System.currentTimeMillis()
		wc.addDir()
		val after = System.currentTimeMillis()

		if (saveResults && wc.isMaterialized) {
			println("Saving results...")
			writeResults(wc.getResult)
		}

		System.gc()
		Thread.sleep(2500)

		wc = null
		wc.close()
		after - before
	}

}


