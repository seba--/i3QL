package sae.playground.remote.iql

import akka.actor.{ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.{BagTable, SetTable}
import idb.operators.impl.{ProjectionView, SelectionView}
import idb.query.{QueryEnvironment, RemoteHost}
import idb.remote.{Added, ObservableHost, RemoteView, SendToRemote}
import sae.example.hospital.data.{HospitalTestData, Patient, Person}
import sae.playground.remote.MultiNodeConfig._
import sae.playground.remote.{MultiNodeConfig, STMultiNodeSpec}

import scala.concurrent.duration.FiniteDuration

/**
  * @author Mirko Köhler
  */
class SimpleQueryTestMultiJvmNode1 extends SimpleQueryTest
class SimpleQueryTestMultiJvmNode2 extends SimpleQueryTest
//class SimpleQueryTestMultiJvmNode3 extends SimpleQueryTest
object SimpleQueryTest {} // this object is necessary for multi-node testing

class SimpleQueryTest extends MultiNodeSpec(HospitalConfig)
	with STMultiNodeSpec with ImplicitSender {

	import HospitalConfig._
	import SimpleQueryTest._

	def initialParticipants = roles.size

	import scala.concurrent.duration._
	override def shutdownTimeout: FiniteDuration = 15.seconds

	"A query" must {
		"compile on 2 different nodes" in {
			runOn(node1) {
				// will run the Table and the Selection (sent from node2)
				val db = SetTable.empty[Person]

				system.actorOf(Props(classOf[ObservableHost[Person]], db), "db") // TODO: provide easier way to create a remotely observable data source
				enterBarrier("deployed")

				enterBarrier("sending")
				Thread.sleep(100) // wait until ObservableHost has its observer registered
				println("has observers: " + db.hasObservers+ ", sending now ...")

				object TestData extends HospitalTestData
				import TestData._

				db += johnDoe
				db += sallyFields

			}

			runOn(node2) {
				// will send the Selection to node 1 and receive the final results
				enterBarrier("deployed")

				implicit val env = QueryEnvironment.create(
					actorSystem = system,
					permissions = Map(
						RemoteHost("personHost", node(node2).address) -> Set("red"),
						RemoteHost("patientHost", node(node1).address) -> Set("red", "blue")
					)
				)

				val remoteHostPath: ActorPath = node(node1) / "user" / "db"


				val db = SetTable.empty[Patient]


                import idb.syntax.iql._
				import idb.syntax.iql.IR._
				import idb.query.colors._

				val personRemote = relation(
					relation = RemoteView[Person](system, remoteHostPath, isSet = true),
					isSet = true,
					color = Color("red"),
					host = RemoteHost("personHost", node(node2).address)
				)

				Predef.println("Starting compile query...")

				val query = SELECT (*) FROM (personRemote, db)
				val compiled : Relation[(Person, Patient)] = idb.syntax.iql.compile (query)

				ObservableHost.forward(compiled, system) // FIXME: always call this on the root node after tree construction (should happen automatically)
				compiled.addObserver(new SendToRemote[(Person, Patient)](testActor))

				Predef.println("Query compiled!")

				enterBarrier("sending")


				object TestData extends HospitalTestData
				import TestData._
				db += patientJohnDoe1

				import scala.concurrent.duration._
				expectMsg(10.seconds, Added((johnDoe, patientJohnDoe1)))
			}

		}
	}

}

// data flows from node1 (Table) -> node2 -> node1 -> node2
/*val tree = RemoteView(
	system,
	node(node1).address,
	new ProjectionView(
		RemoteView(
			system,
			node(node2).address,
			new SelectionView(
				RemoteView[Int](system, remoteHostPath, false),
				fun,
				false
			)
		),
		(n:Int) => { n.toString * 2 },
		false
	)
)     */

