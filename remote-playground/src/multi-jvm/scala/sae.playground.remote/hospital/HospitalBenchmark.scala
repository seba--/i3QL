package sae.playground.remote.hospital

import java.lang.management.ManagementFactory

import idb.{BagTable, Table}
import idb.query.{QueryEnvironment, RemoteHost}
import sae.example.hospital.data.{HospitalSchema, HospitalTestData}

/**
  * Created by mirko on 06.10.16.
  */
trait HospitalBenchmark extends HospitalConfig with CSVPrinter {

	implicit val env : QueryEnvironment

	val waitForWarmup = 5000 //ms
	val waitForMeasure = 5000 //ms

	val waitForSendPerson = 5000 //ms

	val waitForGc = 5000 //ms

	val cpuTimeMeasurements = 50 //ms


	object BaseHospital extends HospitalSchema {
		override val IR = idb.syntax.iql.IR
	}

	object Data extends HospitalTestData

	protected def barrier(name : String)

	protected def gc(): Unit = {
		Thread.sleep(waitForGc)
		System.gc()
	}

	trait DBNode[Domain] {

		val dbName : String
		val waitBeforeSend : Long

		val _warmupIterations : Int
		val _measureIterations : Int

		def iteration(db : Table[Domain], index : Int)

		var finished = false

		def exec(): Unit = {
			import idb.syntax.iql._



			val db = BagTable.empty[Domain]
			REMOTE DEFINE (db, dbName)

			barrier("deployed")

			Thread.sleep(20000)
			//The query gets compiled here...
			barrier("compiled")
			Thread.sleep(waitBeforeSend)
			(1 to _warmupIterations).foreach(i => iteration(db, i))

			barrier("sent-warmup")
			Console.out.println("Wait for warmup...")
			Thread.sleep(waitForWarmup)

			barrier("resetted")

			val thr = new Thread(new Runnable {
				override def run(): Unit = {
					val myOsBean= ManagementFactory.getOperatingSystemMXBean.asInstanceOf[com.sun.management.OperatingSystemMXBean]

					while (!finished) {
						Thread.sleep(cpuTimeMeasurements)
						appendCpu(dbName, System.currentTimeMillis(), myOsBean.getProcessCpuTime(), myOsBean.getProcessCpuLoad())
					}
				}
			})

			gc()
			val rt = Runtime.getRuntime
			val memBefore = rt.totalMemory() - rt.freeMemory()

			thr.start()

			barrier("ready-measure")
			Thread.sleep(waitBeforeSend)
			(1 to _measureIterations).foreach(i => iteration(db, i))

			barrier("sent-measure")
			Console.out.println("Wait for measure...")
			Thread.sleep(waitForMeasure)
			finished = true
			gc()
			val memAfter = rt.totalMemory() - rt.freeMemory()

			barrier("finished")
			appendMemory(dbName,System.currentTimeMillis(),memBefore,memAfter)


		}
	}

}
