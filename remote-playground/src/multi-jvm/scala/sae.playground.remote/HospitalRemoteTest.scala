package sae.playground.remote

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.{BagTable, algebra}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.algebra.demo.RelationalAlgebraDemoPrintPlan
import idb.query.taint._
import idb.query.{QueryEnvironment, RemoteHost}
import idb.schema.hospital._
import idb.syntax.iql
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

	object Data extends HospitalDemoData
	import Data._

	implicit val env = QueryEnvironment.create(
		system,
		Map(


			/***********************************************
			 *** DEFINE PERMISSIONS HERE *******************
			 ***********************************************/

			personHost -> (1, Set("red")),
			patientHost -> (1, Set("red", "green", "purple")),
			knowledgeHost -> (1, Set("purple")),
			clientHost -> (0, Set("red", "green", "purple"))

			/***********************************************
  		 ***********************************************/

		)
	)

	"" must {
		"" in {
			/*
				Person Server
			 */
			runOn(node1) {
				import idb.syntax.iql._

				val db = BagTable.empty[Person]
				REMOTE DEFINE (db, "person-db")

				Thread.sleep(5000)

				enterBarrier("deployed")
				//The query gets compiled here...
				enterBarrier("compiled")

				db += johnDoe
				db += sallyFields
				db += johnCarter
				db += janeDoe

				Thread.sleep(5000)

				enterBarrier("finished")
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
				//The query gets compiled here...
				enterBarrier("compiled")

				db += patientJohnDoe2
				db += patientSallyFields1
				db += patientJohnCarter1
				db += patientJaneDoe1
				db += patientJaneDoe2

				Thread.sleep(5000)

				enterBarrier("finished")
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
				//The query gets compiled here...
				enterBarrier("compiled")

				db += allergy1
				db += allergy2
				db += commonCold1
				db += panicDisorder1

				Thread.sleep(5000)

				enterBarrier("finished")
			}

			/*
				Client
			 */
			runOn(node4) {
				enterBarrier("deployed")

				import idb.syntax.iql._
				import IR._

				//Create variables for all the remote tables
				val personDB : Rep[Query[Person]] =
					REMOTE GET [Person] (personHost, "person-db", Taint("red"))
				val patientDB : Rep[Query[Patient]] =
					REMOTE GET [Patient] (patientHost, "patient-db", Taint("green"))
				val knowledgeDB : Rep[Query[KnowledgeData]] =
					REMOTE GET [KnowledgeData] (knowledgeHost, "knowledge-db", Taint("purple"))




				/***********************************************
         *** DEFINE QUERY HERE *************************
         ***********************************************/
				val q1 =
					SELECT DISTINCT (
						(person: Rep[Person], patientSymptom: Rep[(Patient, String)], knowledgeData: Rep[KnowledgeData]) => (person.personId, person.name, knowledgeData.diagnosis)
					) FROM (
						personDB, UNNEST(patientDB, (x: Rep[Patient]) => x.symptoms), knowledgeDB
					) WHERE	(
						(person: Rep[Person], patientSymptom: Rep[(Patient, String)], knowledgeData: Rep[KnowledgeData]) =>
								person.personId == patientSymptom._1.personId AND
								patientSymptom._2 == knowledgeData.symptom
					)

				/***********************************************
				 ***********************************************/














				//... and add ROOT.
				Thread.sleep(5000)
				val q = ROOT(clientHost, q1)


				//Compile the LMS tree and then materialize for further testing purposes
				val r : idb.Relation[_] = q
				//Print the incoming events
				PrintEvents(r, "result")
				Thread.sleep(15000)

        Predef.println()
        Predef.println()
        Predef.println()
        Predef.println()
        Predef.println("***********************************************")
        Predef.println("*** RECEIVED EVENTS ***************************")
        Predef.println("***********************************************")
        Predef.println()

  			enterBarrier("compiled")
				//The tables are now sending data

				Thread.sleep(5000)

				enterBarrier("finished")

        Predef.println()
        Predef.println("***********************************************")
        Predef.println("***********************************************")
        Predef.println()
        Predef.println()
        Predef.println()
        Predef.println()

			}

		}
	}
}

