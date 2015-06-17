package idb.demo

import java.io.{FileWriter, BufferedWriter}
import java.nio.file.{Path, Paths}

import idb.Relation
import idb.demo.count.WordCountFactory
import idb.demo.routine.{DTAKernKorpusRoutine, ChangeRoutineFactory, ChangeRoutine}
import idb.syntax.iql.compilation.CompilerBinding

/**
 * @author Mirko Köhler
 */
class WordCountBenchmark(val wordcountFactory : WordCountFactory, val routineFactory : ChangeRoutineFactory) {

	private def create(isMaterialized : Boolean) : ChangeRoutine = routineFactory.create(wordcountFactory.create(isMaterialized))

	val warmupIterations = 2
	val measureIterations = 2

	val storeResultsInTextFiles = true

	def run(): Unit = {

		//First iteration with saving of results
		println("Running benchmark for " + wordcountFactory.getClass.getSimpleName)
		println("--------------")
		println("I. Initial run")
		println("--------------")
		val time = executeWordCount(saveResults = storeResultsInTextFiles)
		println(s" -> Warmup Time (ms): $time")


		println("----------")
		println("II. Warmup")
		println("----------")
		for (i <- 2 to warmupIterations) {
			println(s"Iteration $i...")

			val time = executeWordCount(saveResults = false)
			println(s" -> Warmup Time (ms): $time")
		}

		println("---------------")
		println("III. Measure...")
		println("---------------")

		for (i <- 1 to measureIterations) {
			println(s"Iteration $i...")

			val time = executeWordCount(saveResults = false)
			println(s" -> Measured Time (ms): $time")
		}


	}

	private def writeResults(rel : Relation[_]): Unit = {
		val fw = new BufferedWriter(new FileWriter(wordcountFactory.getClass.getSimpleName + "_results.txt"))
		rel.foreach(x => fw.write(s"$x\n"))
		fw.close()
	}

	private def executeWordCount(saveResults : Boolean): List[Long] = {
		System.gc()
		Thread.sleep(3000)
		println(s"Free/total memory: ${Runtime.getRuntime.freeMemory() / 1000000}mB/${Runtime.getRuntime.totalMemory() / 1000000}mB")

		var changeRoutine = create(saveResults)
		var loop = true

		while(loop) {
			System.gc()
			Thread.sleep(3000)
			changeRoutine.initialize()

			changeRoutine.process()

			if (changeRoutine.hasNext)
				changeRoutine.next()
			else
				loop = false
		}


		println("Run finished...")

		if (saveResults && changeRoutine.wordcount.isMaterialized) {
			println("Saving results...")
			writeResults(changeRoutine.wordcount.getResult)
		}

		val res = changeRoutine.getTimes

		changeRoutine = null
		CompilerBinding.reset

		res
	}

}


