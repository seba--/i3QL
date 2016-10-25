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
	override val benchmarkNumber: Int = 3

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
			personHost -> Set("red"),
			patientHost -> Set("red", "green", "purple"),
			knowledgeHost -> Set("purple"),
			clientHost -> Set("white") //For now: Client has its own permission to simulate pushing queries down
		)
	)

	def internalBarrier(name : String): Unit = {
		enterBarrier(name)
	}

	object ClientNode extends ReceiveNode[ResultType] {
		override def relation(): idb.Relation[ResultType] = {
			//Write an i3ql query...
			import idb.syntax.iql._
			import idb.syntax.iql.IR._

			import BaseHospital._
			import Data._

			val personDB : Rep[Query[PersonType]] =
				REMOTE GET (personHost, "person-db", Color("red"))
			val patientDB : Rep[Query[PatientType]] =
				REMOTE GET (patientHost, "patient-db", Color("green"))
			val knowledgeDB : Rep[Query[KnowledgeType]] =
				REMOTE GET (knowledgeHost, "knowledge-db", Color("purple"))

			val q1 =
				SELECT DISTINCT (
					(person: Rep[PersonType], patientSymptom: Rep[(PatientType, String)], knowledgeData: Rep[KnowledgeType]) => (person._1, person._2.personId, person._2.name, knowledgeData.diagnosis)
					) FROM (
					RECLASS(personDB, Color("white")), UNNEST(RECLASS (patientDB, Color("white")), (x: Rep[PatientType]) => x.symptoms), RECLASS (knowledgeDB, Color("white"))
					) WHERE	(
					(person: Rep[PersonType], patientSymptom: Rep[(PatientType, String)], knowledgeData: Rep[KnowledgeType]) =>
						person._2.personId == patientSymptom._1.personId AND
							patientSymptom._2 == knowledgeData.symptom AND
							knowledgeData.symptom == Symptoms.cough AND
							person._2.name == "John Doe"
					)

			//Print the LMS tree representation
			val printer = new RelationalAlgebraPrintPlan {
				override val IR = idb.syntax.iql.IR
			}
			Predef.println("Relation.tree#" + printer.quoteRelation(q1))

			//... and add ROOT. Workaround: Reclass the data to make it pushable to the client node.
			val r : idb.syntax.iql.IR.Relation[ResultType] =
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


