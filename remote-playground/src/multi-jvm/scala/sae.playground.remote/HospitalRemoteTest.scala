package sae.playground.remote

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.BagTable
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.colors._
import idb.query.{QueryEnvironment, RemoteHost}
import idb.schema.hospital._
import idb.util.PrintEvents
import sae.benchmark.hospital.HospitalMultiNodeConfig

class HospitalRemoteTestMultiJvmNode1 extends HospitalRemoteTest
class HospitalRemoteTestMultiJvmNode2 extends HospitalRemoteTest
class HospitalRemoteTestMultiJvmNode3 extends HospitalRemoteTest
class HospitalRemoteTestMultiJvmNode4 extends HospitalRemoteTest

object HospitalRemoteTest {} // this object is necessary for multi-node testing

class HospitalRemoteTest extends MultiNodeSpec(HospitalMultiNodeConfig)
	with STMultiNodeSpec with ImplicitSender {

	import HospitalMultiNodeConfig._

	def initialParticipants = roles.size

	//Setup query environment
	val personHost = RemoteHost("personHost", node(node1))
	val patientHost = RemoteHost("patientHost", node(node2))
	val knowledgeHost = RemoteHost("knowledgeHost", node(node3))
	val clientHost = RemoteHost("clientHost", node(node4))

	object BaseHospital extends HospitalSchema {
		override val IR = idb.syntax.iql.IR
	}
	import BaseHospital._

	object Data extends HospitalTestData
	import Data._

	implicit val env = QueryEnvironment.create(
		system,
		Map(
			personHost -> (1, Set("red")),
			patientHost -> (1, Set("red", "green", "purple")),
			knowledgeHost -> (1, Set("purple")),
			clientHost -> (0, Set("red", "green", "purple")) //For now: Client has its own permission to simulate pushing queries down
		)
	)

	"A hospital" must {
		"work for three servers (without client)" in {
			/*
				Person Server
			 */
			runOn(node1) {
				import idb.syntax.iql._

				val db = BagTable.empty[Person]
				REMOTE DEFINE (db, "person-db")

				Thread.sleep(5000)

				enterBarrier("deployed")
				Predef.println("### DEPLOYED ###")
				//The query gets compiled here...
				enterBarrier("compiled")
				Predef.println("### COMPILED ###")

				db += johnDoe
				db += sallyFields
				db += johnCarter
				db += janeDoe

				Thread.sleep(10000)

				enterBarrier("finished")
				Predef.println("### FINISHED ###")
			}

			/*
				Patient Server
			 */
			runOn(node2) {
				import idb.syntax.iql._

				val db = BagTable.empty[Patient]
				REMOTE DEFINE (db, "patient-db")

				Thread.sleep(5000)

				enterBarrier("deployed")
				Predef.println("### DEPLOYED ###")
				//The query gets compiled here...
				enterBarrier("compiled")
				Predef.println("### COMPILED ###")

				db += patientJohnDoe2
				db += patientSallyFields1
				db += patientJohnCarter1
				db += patientJaneDoe1
				db += patientJaneDoe2

				Thread.sleep(10000)

				enterBarrier("finished")
				Predef.println("### FINISHED ###")
			}

			/*
				Knowledge Server
			 */
			runOn(node3) {
				import idb.syntax.iql._

				val db = BagTable.empty[KnowledgeData]
				REMOTE DEFINE (db, "knowledge-db")

				Thread.sleep(5000)

				enterBarrier("deployed")
				Predef.println("### DEPLOYED ###")
				//The query gets compiled here...
				enterBarrier("compiled")
				Predef.println("### COMPILED ###")

				db += lungCancer1
				db += lungCancer2
				db += commonCold1
				db += panicDisorder1

				Thread.sleep(10000)

				enterBarrier("finished")
				Predef.println("### FINISHED ###")
			}

			/*
				Client
			 */
			runOn(node4) {
				enterBarrier("deployed")
				Predef.println("### DEPLOYED ###")

				import idb.syntax.iql.IR._
				import idb.syntax.iql._

				//Create variables for all the remote tables
				val personDB : Rep[Query[Person]] =
					REMOTE GET [Person] (personHost, "person-db", Color("red"))
				val patientDB : Rep[Query[Patient]] =
					REMOTE GET [Patient] (patientHost, "patient-db", Color("green"))
				val knowledgeDB : Rep[Query[KnowledgeData]] =
					REMOTE GET [KnowledgeData] (knowledgeHost, "knowledge-db", Color("purple"))

				//Write an i3ql query...
				val q1 =
					SELECT DISTINCT (
						(person: Rep[Person], patientSymptom: Rep[(Patient, String)], knowledgeData: Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis)
					) FROM (
						personDB, UNNEST(patientDB, (x: Rep[Patient]) => x.symptoms), knowledgeDB
					) WHERE	(
						(person: Rep[Person], patientSymptom: Rep[(Patient, String)], knowledgeData: Rep[KnowledgeData]) =>
								person.personId == patientSymptom._1.personId AND
								patientSymptom._2 == knowledgeData.symptom //AND
								//knowledgeData.symptom == Symptoms.cough
					)

//				val q1 =
//					SELECT DISTINCT (
//						(person: Rep[Person], patient: Rep[Patient]) => (person.personId, person.name)
//					) FROM (
//						personDB, patientDB
//					) WHERE	(
//						(person: Rep[Person], patient: Rep[Patient]) =>
//								person.personId == patient.personId
//					)

//				val personPath: ActorPath = node(node1) / "user" / "person-db"
//				val persons = RemoteUtils.from[Person](personPath)
//
//				val patientPath: ActorPath = node(node2) / "user" / "patient-db"
//				val patients = RemoteUtils.from[Patient](patientPath)
//
//				val r1 = EquiJoinView(persons, patients, Seq((p : Person) => p.personId), Seq((p : Patient) => p.personId),false)
//				val r2 = DuplicateEliminationView(r1, false)
//
//				val q1 = r2

				//Print the LMS tree representation
				val printer = new RelationalAlgebraPrintPlan {
					override val IR = idb.syntax.iql.IR
				}
				Predef.println(printer.quoteRelation(q1))

				//... and add ROOT. Workaround: Reclass the data to make it pushable to the client node.
				Thread.sleep(10000)
				val q = ROOT(clientHost, q1)


				//Compile the LMS tree and then materialize for further testing purposes
				val r : idb.Relation[_] = q
				PrintEvents(r, "result")
				Thread.sleep(5000)



				enterBarrier("compiled")
				Predef.println("### COMPILED ###")
				//The tables are now sending data

				Thread.sleep(10000)

				enterBarrier("finished")
				Predef.println("### FINISHED ###")

			}

		}
	}
}

