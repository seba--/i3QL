package sae.playground.remote.hospital

import java.io.FileOutputStream
import java.text.SimpleDateFormat

import idb.evaluator.BenchmarkEvaluator

/**
  * Created by mirko on 06.10.16.
  */
trait CSVPrinter extends BenchmarkConfig {

	private val path = "./benchmark/hospital/"

	def printCSV(eval : BenchmarkEvaluator[_]): Unit = {

		val summary@(totalEvents, measuredEvents, timeToReceive, messageDelay) = eval.getSummary

		val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
		val currentDate = sdf.format(new java.util.Date())

		val s = s"### Benchmark $currentDate ###\n" +
			s"iterations=$measureIterations (of which warmup=$warmupIterations), received events=$totalEvents\n" +
			s"Measurement: measured events=$measuredEvents, avg delay=${messageDelay}ms, time to receive=${timeToReceive}ms"

		Predef.println(s)


		//Append to summary file
		{
			val fileName = "benchmark#summary.csv"

			val out: java.io.PrintStream = new java.io.PrintStream(new FileOutputStream(path + fileName, true))
			val csv = s"$currentDate,$benchmarkName,$measureIterations,$warmupIterations,$totalEvents,$measuredEvents,$messageDelay,$timeToReceive"
			out.println(csv)
			out.close()
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
}
