package sae.playground.remote.hospital

import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.util.Date

import akka.actor.{ActorPath, Address, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.{BagTable, Table}
import idb.algebra.ir.{RelationalAlgebraIRBasicOperators, _}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.operations.{OptionOpsExp, SeqOpsExpExt, StringOpsExpExt}
import idb.operators.impl.{ProjectionView, SelectionView}
import idb.query.{QueryEnvironment, RemoteHost}
import idb.remote._
import idb.query._
import idb.query.colors._
import sae.example.hospital.data._
import sae.playground.remote.STMultiNodeSpec

import scala.virtualization.lms.common.{ScalaOpsPkgExp, StaticDataExp, StructExp, TupledFunctionsExp}

class HospitalBenchmark3MultiJvmNode1 extends HospitalBenchmark3
class HospitalBenchmark3MultiJvmNode2 extends HospitalBenchmark3
class HospitalBenchmark3MultiJvmNode3 extends HospitalBenchmark3
class HospitalBenchmark3MultiJvmNode4 extends HospitalBenchmark3

object HospitalBenchmark3 {} // this object is necessary for multi-node testing

//Selection is NOT pushed down == events do NOT get filtered before getting sent
class HospitalBenchmark3 extends MultiNodeSpec(HospitalMultiNodeConfig)
	with STMultiNodeSpec with ImplicitSender with HospitalBenchmark
	//Specifies the number of measurements/warmups
	with BenchmarkConfig1 {

	override val benchmarkName = "hospital3"
	override val benchmarkNumber: Int = 1

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

	def internalBarrier(name : String): Unit = {
		enterBarrier(name)
	}

	object PersonDBNode extends DBNode[PersonType] {
		override val dbName: String = "person-db"

		override val nodeWarmupIterations: Int = warmupIterations
		override val nodeMeasureIterations: Int = measureIterations

		override val isPredata : Boolean = false

		override def iteration(db : Table[(Long, Person)], index : Int): Unit = {
			db += ((System.currentTimeMillis(), sae.example.hospital.data.Person(index, "John Doe", 1973)))
			db += ((System.currentTimeMillis(), sae.example.hospital.data.Person(-index, "Jane Doe", 1960)))
		}
	}

	object PatientDBNode extends DBNode[PatientType] {
		override val dbName: String = "patient-db"

		override val nodeWarmupIterations: Int = warmupIterations
		override val nodeMeasureIterations: Int = measureIterations

		override val isPredata : Boolean = true

		override def iteration(db : Table[(Long, Patient)], index : Int): Unit = {
			db += ((System.currentTimeMillis(),  sae.example.hospital.data.Patient(index, 4, 2011, Seq(Symptoms.cough, Symptoms.chestPain))))
		}
	}

	object KnowledgeDBNode extends DBNode[KnowledgeType] {
		override val dbName: String = "knowledge-db"

		override val nodeWarmupIterations: Int = 1
		override val nodeMeasureIterations: Int = 1

		override val isPredata : Boolean = true

		override def iteration(db : Table[(Long, KnowledgeData)], index : Int): Unit = {
			db += ((System.currentTimeMillis(), lungCancer1))
		}
	}

	object ClientNode extends ReceiveNode[(Long, Long, Long, Int, String)] {
		override def relation(): idb.Relation[(Long, Long, Long, Int, String)] = {
			//Write an i3ql query...
			import idb.syntax.iql._
			import idb.syntax.iql.IR._

			val personDB : Rep[Query[(Long, Person)]] =
				REMOTE GET (personHost, "person-db", Color("red"))
			val patientDB : Rep[Query[(Long, Patient)]] =
				REMOTE GET (patientHost, "patient-db", Color("green"))
			val knowledgeDB : Rep[Query[(Long, KnowledgeData)]] =
				REMOTE GET (knowledgeHost, "knowledge-db", Color("purple"))

			//Write an i3ql query...
			val q1 =
			SELECT DISTINCT (
				(person: Rep[(Long, Person)], patientSymptom: Rep[((Long, Patient), String)], knowledgeData: Rep[(Long, KnowledgeData)]) => (person._1, patientSymptom._1._1, knowledgeData._1, person._2.personId, knowledgeData._2.diagnosis)
				) FROM (
				RECLASS(personDB, Color("white")), UNNEST(RECLASS(patientDB, Color("white")), (x: Rep[(Long, Patient)]) => x._2.symptoms), RECLASS(knowledgeDB, Color("white"))
				) WHERE	(
				(person: Rep[(Long, Person)], patientSymptom: Rep[((Long, Patient), String)], knowledgeData: Rep[(Long, KnowledgeData)]) =>
					person._2.personId == patientSymptom._1._2.personId AND
						patientSymptom._2 == knowledgeData._2.symptom AND
						knowledgeData._2.symptom != Symptoms.chestPain AND
						"Jane Doe" != person._2.name
				)


			//Print the LMS tree representation
			val printer = new RelationalAlgebraPrintPlan {
				override val IR = idb.syntax.iql.IR
			}
			Predef.println("Relation.tree#" + printer.quoteRelation(q1))

			//... and add ROOT. Workaround: Reclass the data to make it pushable to the client node.
			val r : Relation[(Long, Long, Long, Int, String)] =
				ROOT(clientHost, q1)
			r
		}

		override def eventStartTime(e: (Long, Long, Long, Int, String)): Long = {
			scala.math.max(e._1, scala.math.max(e._2, e._3))
		}
	}

	"Hospital Benchmark" must {
		"run benchmark" in {
			runOn(node1) { PersonDBNode.exec() }
			runOn(node2) { PatientDBNode.exec()	}
			runOn(node3) { KnowledgeDBNode.exec() }
			runOn(node4) { ClientNode.exec() }
		}
	}
}


