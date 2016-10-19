package sae.playground.remote.hospital

import akka.actor.{ActorPath, Address, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.BagTable
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

class HospitalRemoteTestMultiJvmNode1 extends HospitalRemoteTest
class HospitalRemoteTestMultiJvmNode2 extends HospitalRemoteTest
class HospitalRemoteTestMultiJvmNode3 extends HospitalRemoteTest
class HospitalRemoteTestMultiJvmNode4 extends HospitalRemoteTest

object HospitalRemoteTest {} // this object is necessary for multi-node testing

class HospitalRemoteTest extends MultiNodeSpec(HospitalMultiNodeConfig)
	with STMultiNodeSpec with ImplicitSender {

	import HospitalMultiNodeConfig._
	import HospitalRemoteTest._

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
			personHost -> Set("red"),
			patientHost -> Set("red", "green", "purple"),
			knowledgeHost -> Set("purple"),
			clientHost -> Set("white") //For now: Client has its own permission to simulate pushing queries down
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

				enterBarrier("deployed")
				//The query gets compiled here...
				enterBarrier("compiled")

				db += johnDoe
				db += sallyFields
				db += johnCarter
				db += janeDoe
			}

			/*
				Patient Server
			 */
			runOn(node2) {
				import idb.syntax.iql._

				val db = BagTable.empty[Patient]
				REMOTE DEFINE (db, "patient-db")

				enterBarrier("deployed")
				//The query gets compiled here...
				enterBarrier("compiled")

				db += patientJohnDoe2
				db += patientSallyFields1
				db += patientJohnCarter1
				db += patientJaneDoe1
				db += patientJaneDoe2
			}

			/*
				Knowledge Server
			 */
			runOn(node3) {
				import idb.syntax.iql._

				val db = BagTable.empty[KnowledgeData]
				REMOTE DEFINE (db, "knowledge-db")

				enterBarrier("deployed")
				//The query gets compiled here...
				enterBarrier("compiled")

				db += lungCancer1
				db += lungCancer2
				db += commonCold1
				db += panicDisorder1
			}

			/*
				Client
			 */
			runOn(node4) {
				enterBarrier("deployed")

				import idb.syntax.iql._
				import idb.syntax.iql.IR._

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
								patientSymptom._2 == knowledgeData.symptom AND
								knowledgeData.symptom == Symptoms.cough
					)

				//... and add ROOT. Workaround: Reclass the data to make it pushable to the client node.
				val q = ROOT(RECLASS(q1, Color("white")), clientHost)


				//Print the LMS tree representation
				val printer = new RelationalAlgebraPrintPlan {
					override val IR = idb.syntax.iql.IR
				}
				Predef.println(printer.quoteRelation(q))

				//Compile the LMS tree and then materialize for further testing purposes
				val r : Relation[_] = q
//				LinkActor.forward(system, r) //TODO: Add this to ROOT
				val relation = r.asMaterialized
				//Print the runtime class representation
				Predef.println(relation.prettyprint(" "))

				//Add observer for testing purposes
				//new RemoteSender(relation, testActor)


				enterBarrier("compiled")
				//The tables are now sending data

				try {
					import scala.concurrent.duration._
					expectMsg(10.seconds, AddedAll(scala.List((0,"John Doe","common cold"), (0,"John Doe","lung cancer"))))
					expectMsg(10.seconds, AddedAll(scala.List((3,"Jane Doe","common cold"), (3,"Jane Doe","lung cancer"))))

				} finally {
					Thread.sleep(3000) //Wait some time until data has been sent
					Predef.println("RELATION:")
					relation.foreach(Predef.println)
				}
			}

		}
	}
}

