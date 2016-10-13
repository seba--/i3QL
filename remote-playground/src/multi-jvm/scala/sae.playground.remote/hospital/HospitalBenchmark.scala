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

	val waitForWarmup = 15 //seconds
	val waitForMeasure = 15 //seconds

	val waitForSendPerson = 10 //seconds

	val waitForGc = 8 //seconds


	object BaseHospital extends HospitalSchema {
		override val IR = idb.syntax.iql.IR
	}

	object Data extends HospitalTestData

	protected def barrier(name : String)

	protected def gc(): Unit = {
		Thread.sleep(waitForGc * 1000)
		System.gc()
	}

	trait DBNode[Domain] {

		val dbName : String
		val waitBeforeSend : Long

		val _warmupIterations : Int
		val _measureIterations : Int

		def iteration(db : Table[Domain], index : Int)

		def exec(): Unit = {
			import idb.syntax.iql._

			val db = BagTable.empty[Domain]
			REMOTE DEFINE (db, dbName)

			barrier("deployed")

			//The query gets compiled here...
			barrier("compiled")
			Thread.sleep(waitBeforeSend)
			(1 to _warmupIterations).foreach(i => iteration(db, i))

			barrier("sent-warmup")

			barrier("resetted")
			gc()
			val rt = Runtime.getRuntime
			val memBefore = rt.totalMemory() - rt.freeMemory()
			val myOsBean= ManagementFactory.getOperatingSystemMXBean.asInstanceOf[com.sun.management.OperatingSystemMXBean]
			appendCpu(dbName, System.currentTimeMillis(), myOsBean.getProcessCpuTime())

			barrier("ready-measure")
			Thread.sleep(waitBeforeSend)
			appendCpu(dbName, System.currentTimeMillis(), myOsBean.getProcessCpuTime())
			(1 to _measureIterations).foreach(i => iteration(db, i))

			barrier("sent-measure")
			Console.out.println("Wait for measure...")
			appendCpu(dbName, System.currentTimeMillis(), myOsBean.getProcessCpuTime())
			Thread.sleep(waitForMeasure * 1000)
			gc()
			val memAfter = rt.totalMemory() - rt.freeMemory()

			barrier("finished")
			appendMemory(dbName,System.currentTimeMillis(),memBefore,memAfter)
		}
	}

}
