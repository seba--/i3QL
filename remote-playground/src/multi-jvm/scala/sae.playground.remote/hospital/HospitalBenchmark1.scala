package sae.playground.remote.hospital

import java.io.FileOutputStream
import java.lang.management.ManagementFactory

import akka.actor.{ActorPath, Address, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.{BagTable, Table}

import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.lms.extensions.operations.{OptionOpsExp, SeqOpsExpExt, StringOpsExpExt}

import idb.query.{QueryEnvironment, RemoteHost}
import idb.query.colors._

import sae.example.hospital.data._
import sae.playground.remote.STMultiNodeSpec

import scala.virtualization.lms.common.{ScalaOpsPkgExp, StaticDataExp, StructExp, TupledFunctionsExp}

class HospitalBenchmark1MultiJvmNode1 extends HospitalBenchmark1
class HospitalBenchmark1MultiJvmNode2 extends HospitalBenchmark1
class HospitalBenchmark1MultiJvmNode3 extends HospitalBenchmark1
class HospitalBenchmark1MultiJvmNode4 extends HospitalBenchmark1

object HospitalBenchmark1 {} // this object is necessary for multi-node testing

//Selection is pushed down == events get filtered before getting sent
class HospitalBenchmark1 extends MultiNodeSpec(HospitalMultiNodeConfig)
	with STMultiNodeSpec with ImplicitSender with HospitalBenchmark {

	override val benchmarkName = "hospital1"

	val warmupIterations = 10000
	val measureIterations = 30000

	import HospitalMultiNodeConfig._
	def initialParticipants = roles.size

	import BaseHospital._
	import Data._

	//Setup query environment
	val personHost = RemoteHost("personHost", node(node1))
	val patientHost = RemoteHost("patientHost", node(node2))
	val knowledgeHost = RemoteHost("knowledgeHost", node(node3))
	val clientHost = RemoteHost("clientHost", node(node4))

	implicit val env = QueryEnvironment.create(
		system,
		Map(
			personHost -> Set("red"),
			patientHost -> Set("red", "green", "purple"),
			knowledgeHost -> Set("purple"),
			clientHost -> Set("white") //For now: Client has its own permission to simulate pushing queries down
		)
	)

	type PersonType = (Long, Person)
	type PatientType = (Long, Patient)
	type KnowledgeType = (Long, KnowledgeData)

	import Data._

	def barrier(name : String): Unit = {
		enterBarrier(name)
	}

	object PersonDBNode extends DBNode[PersonType] {
		override val dbName: String = "person-db"
		override val waitBeforeSend: Long = waitForSendPerson * 1000

		override val _warmupIterations: Int = warmupIterations
		override val _measureIterations: Int = measureIterations

		override def iteration(db : Table[(Long, Person)], index : Int): Unit = {
			db += ((System.currentTimeMillis(), sae.example.hospital.data.Person(index, "John Doe", 1973)))
			db += ((System.currentTimeMillis(), sae.example.hospital.data.Person(index * 2, "Jane Doe", 1960)))
		}
	}

	object PatientDBNode extends DBNode[PatientType] {
		override val dbName: String = "patient-db"
		override val waitBeforeSend: Long = waitForSendPerson * 1000

		override val _warmupIterations: Int = warmupIterations
		override val _measureIterations: Int = measureIterations

		override def iteration(db : Table[(Long, Patient)], index : Int): Unit = {
			db += ((System.currentTimeMillis(),  sae.example.hospital.data.Patient(index, 4, 2011, Seq(Symptoms.cough, Symptoms.chestPain))))
		}
	}

	object KnowledgeDBNode extends DBNode[KnowledgeType] {
		override val dbName: String = "knowledge-db"
		override val waitBeforeSend: Long = waitForSendPerson * 1000

		override val _warmupIterations: Int = 1
		override val _measureIterations: Int = 1

		override def iteration(db : Table[(Long, KnowledgeData)], index : Int): Unit = {
			db += ((System.currentTimeMillis(), lungCancer1))
		}
	}

	"Hospital Benchmark" must {
		"run benchmark" in {
			runOn(node1) { PersonDBNode.exec() }
			runOn(node2) { PatientDBNode.exec()	}
			runOn(node3) { KnowledgeDBNode.exec() }

			runOn(node4) {
				appendTitle()
				enterBarrier("deployed")

				//Write an i3ql query...
				import idb.syntax.iql._
				import idb.syntax.iql.IR._

				val personDB : Rep[Query[(Long, Person)]] =
					REMOTE GET (personHost, "person-db", Color("red"))
				val patientDB : Rep[Query[(Long, Patient)]] =
					REMOTE GET (patientHost, "patient-db", Color("green"))
				val knowledgeDB : Rep[Query[(Long, KnowledgeData)]] =
					REMOTE GET (knowledgeHost, "knowledge-db", Color("purple"))

				val q1 =SELECT DISTINCT (
					(person: Rep[(Long, Person)], patientSymptom: Rep[((Long, Patient), String)], knowledgeData: Rep[(Long, KnowledgeData)]) => (person._1, patientSymptom._1._1, knowledgeData._1, person._2.personId, knowledgeData._2.diagnosis)
				) FROM (
					personDB, UNNEST(patientDB, (x: Rep[(Long, Patient)]) => x._2.symptoms), knowledgeDB
				) WHERE	(
					(person: Rep[(Long, Person)], patientSymptom: Rep[((Long, Patient), String)], knowledgeData: Rep[(Long, KnowledgeData)]) =>
						person._2.personId == patientSymptom._1._2.personId AND
							patientSymptom._2 == knowledgeData._2.symptom AND
							knowledgeData._2.symptom == Symptoms.cough AND
							person._2.name == "John Doe"

					)

				//Print the LMS tree representation
				val printer = new RelationalAlgebraPrintPlan {
					override val IR = idb.syntax.iql.IR
				}
				Predef.println("Relation.tree#" + printer.quoteRelation(q1))

				import idb.syntax.iql._
				import idb.syntax.iql.IR._

				//... and add ROOT. Workaround: Reclass the data to make it pushable to the client node.
				val r : Relation[(Long, Long, Long, Int, String)] =
				ROOT(RECLASS(q1, Color("white")), clientHost)


				//Print the runtime class representation
				Predef.println("Relation.compiled#" + r.prettyprint(" "))


				enterBarrier("compiled")
				//The tables are now sending data
				enterBarrier("sent-warmup")

				Console.out.println("Wait for warmup...")
				Thread.sleep(waitForWarmup * 1000)
				r._reset()
				Console.out.println("Wait for reset...")
				Thread.sleep(3000)

				var clientFinished = false

				enterBarrier("resetted")
				val thr = new Thread(new Runnable {
					override def run(): Unit = {
						val myOsBean= ManagementFactory.getOperatingSystemMXBean.asInstanceOf[com.sun.management.OperatingSystemMXBean]

						while (!clientFinished) {
							Thread.sleep(cpuTimeMeasurements)
							appendCpu("client", System.currentTimeMillis(), myOsBean.getProcessCpuTime(), myOsBean.getProcessCpuLoad())
						}
					}
				})

				gc()

				val rt = Runtime.getRuntime
				val memBefore = rt.totalMemory() - rt.freeMemory()

				thr.start()


				//Add observer for testing purposes
				import idb.evaluator.BenchmarkEvaluator
				val benchmark = new BenchmarkEvaluator[(Long, Long, Long, Int, String)](r, t => scala.math.max(t._1, scala.math.max(t._2, t._3)), measureIterations, 0)

				enterBarrier("ready-measure")

				// /The tables are now sending data
				enterBarrier("sent-measure")

				Console.out.println("Wait for measure...")
				Thread.sleep(waitForMeasure * 1000)
				clientFinished = true
				gc()
				val memAfter = rt.totalMemory() - rt.freeMemory()

				appendSummary(benchmark)

				enterBarrier("finished")
				appendMemory("client",System.currentTimeMillis(),memBefore,memAfter)
			}
		}
	}
}

