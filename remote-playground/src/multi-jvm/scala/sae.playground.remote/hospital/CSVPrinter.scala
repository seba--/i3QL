package sae.playground.remote.hospital

import java.io.{File, FileOutputStream}
import java.text.SimpleDateFormat

import idb.evaluator.BenchmarkEvaluator

/**
  * Created by mirko on 06.10.16.
  */
trait CSVPrinter extends HospitalConfig {

	private val path = "./benchmark/hospital/"
	private val summaryFileName = "benchmark#summary.csv"
	private val summaryFile = new File(path + summaryFileName)


	def appendTitle(): Unit = {
		addToSummaryFile(s"benchmark,$benchmarkName,$measureIterations,$warmupIterations")
	}

	def appendSummary(eval : BenchmarkEvaluator[_]): Unit = {

		val summary@(totalEvents, measuredEvents, timeToReceive, messageDelay) = eval.getSummary
		val currentDate = System.currentTimeMillis()

		val s = s"### Benchmark $currentDate ###\n" +
			s"iterations=$measureIterations (of which warmup=$warmupIterations), received events=$totalEvents\n" +
			s"Measurement: measured events=$measuredEvents, avg delay=${messageDelay}ms, time to receive=${timeToReceive}ms"

		Predef.println(s)


		//Append to summary file
		{
			val csv = s"result,$currentDate,$totalEvents,$measuredEvents,$messageDelay,$timeToReceive"
			addToSummaryFile(csv)
		}

		//Write file for this benchmark only
		{
			val fileName = s"benchmark#$currentDate#$benchmarkName#$measureIterations.csv"

			val out: java.io.PrintStream = new java.io.PrintStream(new FileOutputStream(path + fileName, false))
			eval.eventTimes.foreach(t => {
				val csv = s"${t._1},${t._2}"
				out.println(csv)
			})
			out.close()
		}
	}

	def appendMemory(node : String, time : Long, memoryBefore : Long, memoryAfter : Long): Unit = {
		addToSummaryFile(s"mem,$node,$time,$memoryBefore,$memoryAfter")
	}

	def appendCpu(node : String, time : Long, cpu : Long, load : Double): Unit = {
		addToSummaryFile(s"cpu,$node,$time,$cpu,$load")
	}

	private def addToSummaryFile(s : String): Unit = {
		val out: java.io.PrintStream = new java.io.PrintStream(new FileOutputStream(summaryFile, true))
		out.println(s)
		out.close()
	}
}
