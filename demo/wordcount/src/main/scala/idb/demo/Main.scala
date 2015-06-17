package idb.demo

import java.io._
import java.nio.file.{Paths, Files}

import idb.demo.count.{WordCountDistinctWordsFactory, WordCountTotalFactory, WordCountPerWordFactory}
import idb.demo.routine.DTAKernKorpusRoutineFactory
import idb.{Relation, BagTable}


/**
 * @author Mirko Köhler
 */
object Main {


	def main(args : Array[String]): Unit = {
		new WordCountBenchmark(wordcountFactory = WordCountPerWordFactory, routineFactory = DTAKernKorpusRoutineFactory).run()
		new WordCountBenchmark(wordcountFactory = WordCountTotalFactory, routineFactory = DTAKernKorpusRoutineFactory).run()
		new WordCountBenchmark(wordcountFactory = WordCountDistinctWordsFactory, routineFactory = DTAKernKorpusRoutineFactory).run()


	}
}
