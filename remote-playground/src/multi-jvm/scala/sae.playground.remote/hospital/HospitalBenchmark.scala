package sae.playground.remote.hospital

import idb.benchmark.Measurement
import idb.{BagTable, Relation, Table}
import idb.query.QueryEnvironment
import sae.example.hospital.data._

/**
  * Barriers that are used in the hospital benchmark:
  *
  * deployed - The tables have been deployed on their servers and the printer has been initialized.
  * compiled - The query has been compiled and deployed to the servers.
  *
  * sent-warmup - The warmup events have been sent (from the tables)
  *
  * resetted - The warmup events have been received and the data structures have been resetted.
  *
  * ready-measure - The classes needed for measurements have been initialized.
  * sent-measure - The measure events have been sent (from the tables).
  *
  * finished - The measurement has been finished.
  *
  *
  * deploy
  * compile
  * warmup-predata
  * warmup-data
  * warmup-finish
  * reset
  * measure-predata
  * measure-init
  * measure-data
  * measure-finish
  * finish
  */
trait HospitalBenchmark extends HospitalConfig with CSVPrinter {

	implicit val env : QueryEnvironment

	val waitForCompile = 20000 //ms
	val waitForData = 20000 //ms
	val waitForReset = 5000 //ms
	val waitForGc = 5000 //ms

	val cpuMeasurementInterval = 50 //ms

	object BaseHospital extends HospitalSchema {
		override val IR = idb.syntax.iql.IR
	}

	object Data extends HospitalTestData
	import Data._

	protected def internalBarrier(name : String)

	private def section(name : String): Unit = {
		internalBarrier(name : String)
		println(s"### Enter barrier __${name}__ ###")
	}

	type PersonType = (Long, Person)
	type PatientType = Patient
	type KnowledgeType = KnowledgeData

	type ResultType = (Long, Int, String, String)



	object PersonDBNode extends DBNode[PersonType] {
		override val dbName: String = "person-db"

		override val nodeWarmupIterations: Int = warmupIterations
		override val nodeMeasureIterations: Int = measureIterations

		override val isPredata : Boolean = false

		override def iteration(db : Table[(Long, Person)], index : Int): Unit = {
			db += ((System.currentTimeMillis(), sae.example.hospital.data.Person(index, "John Doe", 1973)))
			db += ((System.currentTimeMillis(), sae.example.hospital.data.Person(index, "Jane Doe", 1960)))
		}
	}

	object PatientDBNode extends DBNode[PatientType] {
		override val dbName: String = "patient-db"

		override val nodeWarmupIterations: Int = warmupIterations
		override val nodeMeasureIterations: Int = measureIterations

		override val isPredata : Boolean = true

		override def iteration(db : Table[Patient], index : Int): Unit = {
			db += sae.example.hospital.data.Patient(index, 4, 2011, Seq(Symptoms.cough, Symptoms.chestPain))
		}
	}

	object KnowledgeDBNode extends DBNode[KnowledgeType] {
		override val dbName: String = "knowledge-db"

		override val nodeWarmupIterations: Int = 1
		override val nodeMeasureIterations: Int = 1

		override val isPredata : Boolean = true

		override def iteration(db : Table[KnowledgeData], index : Int): Unit = {
			db += lungCancer1
		}
	}



	trait DBNode[Domain] {

		val dbName : String

		val nodeWarmupIterations : Int
		val nodeMeasureIterations : Int

		val isPredata : Boolean

		def iteration(db : Table[Domain], index : Int)

		var finished = false

		def exec(): Unit = {

			section("deploy")
			import idb.syntax.iql._
			val db = BagTable.empty[Domain]
			REMOTE DEFINE (db, dbName)

			section("compile")
			//The query gets compiled here...

			section("warmup-predata")
			if (isPredata) {
				(1 to nodeWarmupIterations).foreach(i => iteration(db, i))
			}

			section("warmup-data")
			if (!isPredata) {
				(1 to nodeWarmupIterations).foreach(i => iteration(db, i))
			}

			section("warmup-finish")

			section("reset")

			section("measure-predata")
			if (isPredata) {
				(1 to nodeMeasureIterations).foreach(i => iteration(db, i))
			}

			section("measure-init")

			Measurement.Memory((memBefore, memAfter) => appendMemory(dbName,System.currentTimeMillis(),memBefore,memAfter), sleepAfterGc = waitForGc) {
				Measurement.CPU((time, cpuTime, cpuLoad) => appendCpu(dbName, time, cpuTime, cpuLoad), interval = cpuMeasurementInterval) {
					section("measure-data")
					(1 to nodeMeasureIterations).foreach(i => iteration(db, i))

					section("measure-finish")
				}
			}

			section("finish")
		}
	}

	trait ReceiveNode[Domain] {

		def relation() : Relation[Domain]
		def eventStartTime(e : Domain) : Long

		var finished = false

		def exec(): Unit = {
			section("deploy")
			init()
			appendTitle()

			section("compile")
			val r : Relation[Domain] = relation()
			//Print the runtime class representation
			Predef.println("Relation.compiled#" + r.prettyprint(" "))
			Thread.sleep(waitForCompile)

			section("warmup-predata")
			//The tables are now sending data
			Thread.sleep(waitForData)

			section("warmup-data")
			//The tables are now sending data
			Thread.sleep(waitForData)

			section("warmup-finish")

			section("reset")
			r.reset()
			Thread.sleep(waitForReset)

			section("measure-predata")
			//The tables are now sending data
			Thread.sleep(waitForData)

			section("measure-init")
			//Add observer for testing purposes
			import idb.benchmark._
			val count = new CountEvaluator(r)
			val delay = new DelayEvaluator(r, eventStartTime, measureIterations)
			val throughput = new ThroughputEvaluator(r, count)

			Measurement.Memory((memBefore, memAfter) => appendMemory("client",System.currentTimeMillis(),memBefore,memAfter), sleepAfterGc = waitForGc)(
				Measurement.CPU((time, cpuTime, cpuLoad) => appendCpu("client", time, cpuTime, cpuLoad), interval = cpuMeasurementInterval) {
					section("measure-data")
					Thread.sleep(waitForData)

					section("measure-finish")
				}
			)

			appendSummary(count, throughput, delay)
			section("finish")

		}
	}

}
