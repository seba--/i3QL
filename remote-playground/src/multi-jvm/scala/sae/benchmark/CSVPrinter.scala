package sae.benchmark

import java.io.{File, FileOutputStream}
import java.text.SimpleDateFormat
import java.util.Date

import idb.benchmark.{CountEvaluator, DelayEvaluator, ThroughputEvaluator}

/**
  * Created by mirko on 06.10.16.
  */
trait CSVPrinter extends BenchmarkConfig {

	private val currentDate : Date = new Date()
	private val dayFormat = new SimpleDateFormat("yyyy-MM-dd")
	private val timeFormat = new SimpleDateFormat("HH:mm:ss.SSS")
	private val benchmarkDay = dayFormat.format(currentDate)
	private val benchmarkTime =  timeFormat.format(currentDate)

	private def path = s"./benchmark/remote/$benchmarkGroup/$benchmarkDay/$benchmarkQuery/${benchmarkType}_${benchmarkConfig}_$benchmarkNumber/"
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

		val h1 = s"type,benchmarkName,time,measureIterations,receivedEvents,entries,averageDelay,medianDelay,timeToReceive,eventsPerSecond"
		addToFile(summaryFile, h1)

		val h2 = s"type,host,time,memoryBefore,memoryAfter"
		addToFile(memoryFile, h2)

		val h3 = s"type,host,time,cpuTime,cpuLoad"
		addToFile(cpuFile, h3)


	}

	def appendTitle(): Unit = {
		//addToFile(summaryFile, s"benchmark,$benchmarkName,$measureIterations,$warmupIterations")
	}

	def appendSummary(countEvaluator: CountEvaluator[_], throughputEvaluator : ThroughputEvaluator[_], delayEvaluator : DelayEvaluator[_]): Unit = {

		val eventCount = countEvaluator.countEvents
		val entriesCount = countEvaluator.countEntries

		val averageDelay = delayEvaluator.averageDelay
		val medianDelay = delayEvaluator.medianDelay
		val throughput = throughputEvaluator.result()


		val s = s"### Benchmark $benchmarkTime ###\n" +
			s"iterations=$measureIterations, received events=$eventCount, entries=$entriesCount\n" +
			s"avg delay=${averageDelay}ms, median delay=${medianDelay}ms, time to receive=${throughput._2}ms, events per second=${throughput._1}"

		Predef.println(s)


		//Append to summary file
		{

			val csv = s"benchmark,$benchmarkQuery,$benchmarkTime,$measureIterations,$eventCount,$entriesCount,$averageDelay,$medianDelay,${throughput._2},${throughput._1}"
			addToFile(summaryFile, csv)
		}

		//Write file for this benchmark only
		{
			val fileName = s"benchmark-msginfo.csv"

			val out: java.io.PrintStream = new java.io.PrintStream(new FileOutputStream(path + fileName, false))
			out.println("sendingTime,receivingTime")
			delayEvaluator.eventTimes.foreach(t => {
				val csv = s"${t._1},${t._2}"
				out.println(csv)
			})
			out.close()
		}
	}

	def appendMemory(node : String, time : Long, memoryBefore : Long, memoryAfter : Long): Unit = {
		addToFile(memoryFile, s"mem,$node,$time,$memoryBefore,$memoryAfter")
	}

	def appendCpu(node : String, time : Long, cpuTime : Long, cpuLoad : Double): Unit = {
		addToFile(cpuFile, s"cpu,$node,$time,$cpuTime,$cpuLoad")
	}

	private def addToFile(f : File, s : String): Unit = {
		val out: java.io.PrintStream = new java.io.PrintStream(new FileOutputStream(f, true))
		out.print(s + "\n")
		out.close()
	}
}
