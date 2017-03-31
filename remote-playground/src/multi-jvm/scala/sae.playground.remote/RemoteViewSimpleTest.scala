package sae.playground.remote

import akka.actor.{ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.{BagTable, remote}
import idb.operators.impl.{DuplicateEliminationView, EquiJoinView, ProjectionView, SelectionView}
import idb.remote
import idb.remote.receive.{RefRemoteReceiver, RemoteReceiver}
import idb.syntax.iql.runtime.RemoteUtils
import idb.util.PrintEvents

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
				val ref = RemoteUtils.create(system)("db", db)
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
				//Local db
				val db = BagTable.empty[Int]
				//Build connection to the table
				val remoteHostPath: ActorPath = node(node1) / "user" / "db"
				val remoteTable = RemoteUtils.from[Int](remoteHostPath)
				//Define local relation...
				val q1 = SelectionView (
					remoteTable,
					(x : Int) => x > 2,
					false
				)
				//and deploy it on the desired node
				val q1Ref = RemoteUtils.deploy(system, node(node2))(q1)

				//Repeat as often as you want...
				val remoteQ1 = RemoteUtils.from[Int](q1Ref)
				val q2 = ProjectionView (
					remoteQ1,
					(x : Int) => x * 2,
					false
				)
				val q2Ref = RemoteUtils.deploy(system, node(node1))(q2)

				val remoteQ2 = RemoteUtils.from[Int](q2Ref)
				val join = EquiJoinView(remoteQ2, db, Seq((i : Int) => i), Seq((i : Int) => i), false)
				val q3 = DuplicateEliminationView(join, false)
				val q3Ref = RemoteUtils.deploy(system, node(node2))(q3)

				//Build connection to the result
				val recv = RemoteUtils.fromWithDeploy(system, q3Ref)
				idb.util.printEvents(recv, "result")

				Thread.sleep(1000) //Wait until all observers have been registered.

				enterBarrier("sending")
				println("### SENDING ###")

				Thread.sleep(8000)

				db += 1
				db += 2
				db += 3
				db += 4
				db += 5
				db += 6
				db += 7
				db += 8


				Thread.sleep(1000) //Wait until sending has finished

				enterBarrier("finished")
				println("### FINISHED ###")
			}

			//enterBarrier("finished")
		}
	}
}

