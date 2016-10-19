package sae.playground.remote

import akka.actor.{ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.{BagTable, remote}
import idb.evaluator.PrintRows
import idb.operators.impl.{ProjectionView, SelectionView}
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
				db += 4

				enterBarrier("finished")
				println("### FINISHED ###")
			}

			runOn(node2) {
				enterBarrier("deployed")
				println("### DEPLOYED ###")

				Thread.sleep(1000)
				//Build connection to the table
				val remoteHostPath: ActorPath = node(node1) / "user" / "db"
				val remoteTable = remote.from[Int](remoteHostPath)
				//Define local relation...
				val q1 = ProjectionView (
					remoteTable,
					(x : Int) => x * 2,
					false
				)
				//and deploy it on the desired node
				val q1Ref = remote.deploy(system, node(node2))(q1)

				//Repeat as often as you want...
				val remoteQ1 = remote.from[Int](q1Ref)
				val q2 = SelectionView (
					remoteQ1,
					(x : Int) => x > 4,
					false
				)
				val q2Ref = remote.deploy(system, node(node1))(q2)


				//Build connection to the result
				val recv = remote.fromWithDeploy(system, q2Ref)
				PrintRows(recv, "result")

				Thread.sleep(1000) //Wait until all observers have been registered.

				enterBarrier("sending")
				println("### SENDING ###")

				Thread.sleep(1000) //Wait until sending has finished

				enterBarrier("finished")
				println("### FINISHED ###")
			}

			//enterBarrier("finished")
		}
	}
}

