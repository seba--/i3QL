package sae.benchmark.hospital

import akka.remote.testkit.MultiNodeSpec
import idb.{Relation, algebra}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.taint._
import idb.query.{QueryEnvironment, RemoteHost}
import idb.syntax.iql.IR
import sae.benchmark.BenchmarkMultiNodeSpec

class HospitalBenchmark1MultiJvmNode1 extends HospitalBenchmark1
class HospitalBenchmark1MultiJvmNode2 extends HospitalBenchmark1
class HospitalBenchmark1MultiJvmNode3 extends HospitalBenchmark1
class HospitalBenchmark1MultiJvmNode4 extends HospitalBenchmark1

object HospitalBenchmark1 {} // this object is necessary for multi-node testing

//Selection is pushed down == events get filtered before getting sent
class HospitalBenchmark1 extends MultiNodeSpec(HospitalMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with DefaultHospitalBenchmark
	//Specifies the number of measurements/warmups
	with Measure50000Config {

	override val benchmarkQuery = "query1"
	override val benchmarkNumber: Int = 5

	import HospitalMultiNodeConfig._
	def initialParticipants = roles.size

	//Setup query environment
	val personHost = RemoteHost("personHost", node(node1))
	val patientHost = RemoteHost("patientHost", node(node2))
	val knowledgeHost = RemoteHost("knowledgeHost", node(node3))
	val clientHost = RemoteHost("clientHost", node(node4))

	implicit val env = QueryEnvironment.create(
		system,
		Map(
			personHost -> (1, Set("red")),
			patientHost -> (1, Set("red", "green", "purple")),
			knowledgeHost -> (1, Set("purple")),
			clientHost -> (4, Set("red", "green", "purple"))
		)
	)

	def internalBarrier(name : String): Unit = {
		enterBarrier(name)
	}

	override type ResultType = (Long, Int, String, String)

	object ClientNode extends ReceiveNode[ResultType] {
		override def relation(): Relation[ResultType] = {
			//Write an i3ql query...
			import BaseHospital._
			import Data._
			import idb.syntax.iql.IR._
			import idb.syntax.iql._

			val personDB : Rep[Query[PersonType]] =
				REMOTE GET (personHost, "person-db", Taint("red"))
			val patientDB : Rep[Query[PatientType]] =
				REMOTE GET (patientHost, "patient-db", Taint("green"))
			val knowledgeDB : Rep[Query[KnowledgeType]] =
				REMOTE GET (knowledgeHost, "knowledge-db", Taint("purple"))

			val q1 =
				SELECT DISTINCT (
					(person: Rep[PersonType], patientSymptom: Rep[(PatientType, String)], knowledgeData: Rep[KnowledgeType]) => (person._1, person._2.personId, person._2.name, knowledgeData.diagnosis)
				) FROM (
					personDB, UNNEST(patientDB, (x: Rep[PatientType]) => x.symptoms), knowledgeDB
				) WHERE	(
					(person: Rep[PersonType], patientSymptom: Rep[(PatientType, String)], knowledgeData: Rep[KnowledgeType]) =>
						person._2.personId == patientSymptom._1.personId AND
						patientSymptom._2 == knowledgeData.symptom AND
						knowledgeData.symptom == Symptoms.cough AND
						person._2.name == "John Doe"
				)

			//... and add ROOT. Workaround: Reclass the data to make it pushable to the client node.
			val r : idb.Relation[ResultType] =
				ROOT(clientHost, q1)
			r
		}

		override def eventStartTime(e: ResultType): Long = {
			e._1
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
