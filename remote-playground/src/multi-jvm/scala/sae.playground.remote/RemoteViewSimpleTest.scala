package sae.playground.remote

import akka.actor.{ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.{BagTable, remote}
import idb.evaluator.PrintRows
import idb.operators.impl.ProjectionView
import idb.remote
import idb.remote.receive.{RefRemoteReceiver, RemoteReceiver}

import scala.concurrent.Await


class RemoteViewSimpleTestMultiJvmNode1 extends RemoteViewSimpleTest
class RemoteViewSimpleTestMultiJvmNode2 extends RemoteViewSimpleTest
object RemoteViewSimpleTest {} // this object is necessary for multi-node testing

class RemoteViewSimpleTest extends MultiNodeSpec(MultiNodeConfig)
with STMultiNodeSpec with ImplicitSender {

	import MultiNodeConfig._
	import RemoteViewSimpleTest._

	def initialParticipants = roles.size

	"A RemoteView" must {
		"receive from a simple ObservableHost" in {
			runOn(node1) {
				val db = BagTable.empty[Int]
				val ref = remote.create(system)("db", db)
				enterBarrier("deployed")
				println("### DEPLOYED ###")

				enterBarrier("sending")
				println("### SENDING ###")

				db += 1
				db += 2
				db += 3

				enterBarrier("finished")
				println("### FINISHED ###")
			}

			runOn(node2) {
				enterBarrier("deployed")
				println("### DEPLOYED ###")

				Thread.sleep(1000)
				val remoteHostPath: ActorPath = node(node1) / "user" / "db"

				val table = remote.from[Int](system)(remoteHostPath)


				val q = ProjectionView (
					table,
					(x : Int) => x * 2,
					false
				)

				val actorRef = remote.deploy(system, node(node2))(q)

				val recv = RefRemoteReceiver(actorRef)
				recv.deploy(system)
				PrintRows(recv, "result")

				Thread.sleep(1000)

				enterBarrier("sending")
				println("### SENDING ###")

				Thread.sleep(1000)

				enterBarrier("finished")
				println("### FINISHED ###")
			}

			//enterBarrier("finished")
		}
	}
}

