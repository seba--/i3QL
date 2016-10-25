package sae.playground.remote.hospital

import java.io.{File, FileOutputStream}
import java.text.SimpleDateFormat
import java.util.Date

import idb.evaluator.BenchmarkEvaluator

/**
  * Created by mirko on 06.10.16.
  */
trait CSVPrinter extends HospitalConfig {

	val benchmarkType : String
	val benchmarkNumber : Int

	private val currentDate : Date = new Date()
	private val dayFormat = new SimpleDateFormat("yyyy-MM-dd")
	private val timeFormat = new SimpleDateFormat("HH:mm:ss.SSS")
	private val benchmarkDay = dayFormat.format(currentDate)
	private val benchmarkTime =  timeFormat.format(currentDate)

	private def path = s"./benchmark/hospital/$benchmarkDay/$benchmarkName-$benchmarkType-$benchmarkNumber/"
	private def summaryFile = new File(path + s"benchmark-summary.csv")
	private def memoryFile = new File(path + s"benchmark-mem.csv")
	private def cpuFile = new File(path + s"benchmark-cpu.csv")

	def init(): Unit = {

		val dir = new File(path)
		if (!dir.exists()) dir.mkdirs()

		val files = List(summaryFile, memoryFile, cpuFile)
		files.foreach(f => {
			f.delete()
			f.createNewFile()
		})


	}

	def appendTitle(): Unit = {
		addToFile(summaryFile, s"benchmark,$benchmarkName,$measureIterations,$warmupIterations")
	}

	def appendSummary(eval : BenchmarkEvaluator[_]): Unit = {

		val summary@(totalEvents, measuredEvents, timeToReceive, messageDelay) = eval.getSummary


		val s = s"### Benchmark $benchmarkTime ###\n" +
			s"iterations=$measureIterations (of which warmup=$warmupIterations), received events=$totalEvents\n" +
			s"Measurement: measured events=$measuredEvents, avg delay=${messageDelay}ms, time to receive=${timeToReceive}ms"

		Predef.println(s)


		//Append to summary file
		{
			val csv = s"result,$benchmarkTime,$totalEvents,$measuredEvents,$messageDelay,$timeToReceive"
			addToFile(summaryFile, csv)
		}

		//Write file for this benchmark only
		{
			val fileName = s"benchmark-msginfo.csv"

			val out: java.io.PrintStream = new java.io.PrintStream(new FileOutputStream(path + fileName, true))
			eval.eventTimes.foreach(t => {
				val csv = s"${t._1},${t._2}"
				out.println(csv)
			})
			out.close()
		}
	}

	def appendMemory(node : String, time : Long, memoryBefore : Long, memoryAfter : Long): Unit = {
		addToFile(memoryFile, s"mem,$node,$time,$memoryBefore,$memoryAfter")
	}

	def appendCpu(node : String, time : Long, cpu : Long, load : Double): Unit = {
		addToFile(cpuFile, s"cpu,$node,$time,$cpu,$load")
	}

	private def addToFile(f : File, s : String): Unit = {
		val out: java.io.PrintStream = new java.io.PrintStream(new FileOutputStream(f, true))
		out.println(s)
		out.close()
	}
}
