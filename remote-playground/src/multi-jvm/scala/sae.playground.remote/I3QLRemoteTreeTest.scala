package sae.playground.remote

import akka.actor.{ActorPath, Address, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.BagTable
import idb.algebra.ir.{RelationalAlgebraIRBasicOperators, _}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.operators.impl.{ProjectionView, SelectionView}
import idb.query.{QueryEnvironment, RemoteHost}
import idb.remote._
import idb.query._
import idb.query.colors._

import scala.virtualization.lms.common.{ScalaOpsPkgExp, StaticDataExp, StructExp, TupledFunctionsExp}

class I3QLRemoteTreeTestMultiJvmNode1 extends I3QLRemoteTreeTest
class I3QLRemoteTreeTestMultiJvmNode2 extends I3QLRemoteTreeTest
object I3QLRemoteTreeTest {} // this object is necessary for multi-node testing

class I3QLRemoteTreeTest extends MultiNodeSpec(MultiNodeConfig)
	with STMultiNodeSpec with ImplicitSender {

	import MultiNodeConfig._
	import I3QLRemoteTreeTest._

	def initialParticipants = roles.size

	//Setup query environment
	val host1 = RemoteHost("node1", node(node1))
	val host2 = RemoteHost("node2", node(node2))

	implicit val env = QueryEnvironment.create(
		system,
		Map(host1 -> Set("red"), host2 -> Set("blue"))
	)

	"A manually generated remote tree" must {
		"work the same as the automatically generated tree" in {
			//enterBarrier("startup") // TODO: is this necessary?

			runOn(node1) {
				// will run the Table and the Selection (sent from node2)

				//system.actorOf(Props(classOf[ObservableHost[Int]], db), "db") // TODO: provide easier way to create a remotely observable data source
				import idb.syntax.iql._

				val db = BagTable.empty[Int]
//				env.system.actorOf(Props(classOf[LinkActor[Int]], db), "db")

				enterBarrier("deployed")

				enterBarrier("sending")
				Thread.sleep(100) // wait until ObservableHost has its observer registered
				println("has observers: " + db.hasObservers+ ", sending now ...")

				db += 1
				db += 2
				db += 3
				db += 4
				db += 5
				db += 6
			}

			runOn(node2) {
				// will send the Selection to node 1 and receive the final results
				enterBarrier("deployed")

				import idb.Relation

//				//FIXME: Why do we have to explicitly specify the type here?
//				val table  = ReceiveView[Int](env.system, node(node1) / "user" / "db", false)
//				//		REMOTE FROM[Int] (host1, "db", Color("red"))
//
//				val q1 = ReceiveView(env.system, node(node2).address,
//					SelectionView(table, (i : Int) => i > 2, false)
//				)
////					RECLASS(
////						SELECT (*) FROM table WHERE ((i : Rep[Int]) => i > 2),
////						Color("blue")
////					)
//
//				val q2 = ReceiveView(env.system, node(node1).address,
//					ProjectionView(q1, (i : Int) => i + 2, false)
//				)
////					RECLASS(
////						SELECT ((i : Rep[Int]) => i + 2) FROM q1,
////						//SELECT (*) FROM q1,
////						Color("red")
////					)
//
//				val q3 = ReceiveView(env.system, node(node2).address, q2)
//				LinkActor.forward(system, q3)
//				//	ROOT(q2, host2)
//
//
//				val relation : Relation[Int] = q3.asMaterialized
//
//				Predef.println(relation.prettyprint(" "))
//
//				// data flows from node1 (Table) -> node2 -> node1 -> node2
//				/*val tree = RemoteView(
//					system,
//					node(node1).address,
//					new ProjectionView(
//						RemoteView(
//							system,
//							node(node2).address,
//							new SelectionView(
//								RemoteView[Int](system, remoteHostPath, false),
//								fun,
//								false
//							)
//						),
//						(n:Int) => { n.toString * 2 },
//						false
//					)
//				)    */
//
//				new RemoteSender[Int](relation, testActor)

				enterBarrier("sending")

				import scala.concurrent.duration._
				expectMsg(10.seconds, Added(5))
				expectMsg(10.seconds, Added(6))
				expectMsg(10.seconds, Added(7))
				expectMsg(10.seconds, Added(8))

				Thread.sleep(7000)

//				relation.foreach(Predef.println)

			}

			//enterBarrier("finished")
		}
	}
}

