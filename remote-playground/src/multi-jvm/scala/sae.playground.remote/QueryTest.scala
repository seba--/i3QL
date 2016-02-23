package sae.playground.remote

import akka.actor.{ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.operators.impl.{CrossProductView, SelectionView}
import idb.{SetTable, BagTable}
import idb.remote._

class QueryTestMultiJvmNode1 extends RemoteViewSimpleTest
class QueryTestMultiJvmNode2 extends RemoteViewSimpleTest
object QueryTest {} // this object is necessary for multi-node testing

class QueryTest extends MultiNodeSpec(MultiNodeConfig)
with STMultiNodeSpec with ImplicitSender {

	import MultiNodeConfig._
	import QueryTest._

	def initialParticipants = roles.size

	"A RemoteView" must {
		"receive from a simple ObservableHost" in {
			//enterBarrier("startup") // TODO: is this necessary?

			runOn(node1) {

				val table1 = SetTable.empty[Int]
				val table2 = SetTable.empty[Int]

				val sel1 = SelectionView(table1, (i : Int) => i % 2 == 0, true)
				val cp1 = CrossProductView(sel1, table2, true)


				system.actorOf(Props(classOf[ObservableHost[String]], db), "db")
				enterBarrier("deployed")

				enterBarrier("sending")
				Thread.sleep(1000) // wait until ObservableHost has its observer registered
				println("has observers: " + db.hasObservers)

				db += "Test1"
				db += "Test2"
			}

			runOn(node2) {
				enterBarrier("deployed")

				val remoteHostPath: ActorPath = node(node1) / "user" / "db"
				val remoteView: RemoteView[String] = RemoteView[String](system, remoteHostPath, false)

				remoteView.addObserver(new SendToRemote[String](testActor))
				enterBarrier("sending")

				import scala.concurrent.duration._
				expectMsg(10.seconds, Added("Test1"))
				expectMsg(10.seconds, Added("Test2"))
			}

			runOn(node3) {

			}

			//enterBarrier("finished")
		}
	}
}


