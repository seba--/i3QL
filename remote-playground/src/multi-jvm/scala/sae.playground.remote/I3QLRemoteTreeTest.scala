package sae.playground.remote

import akka.actor.{ActorPath, Address, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.{BagTable, algebra, remote}
import idb.algebra.ir.{RelationalAlgebraIRBasicOperators, _}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.operators.impl.{ProjectionView, SelectionView}
import idb.query.{QueryEnvironment, RemoteHost}
import idb.remote._
import idb.query._
import idb.query.taint._
import idb.syntax.iql.IR
import idb.syntax.iql.runtime.RemoteUtils
import idb.util.PrintEvents

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
		Map(host1 -> (0, Set("red")), host2 -> (0, Set("blue")))
	)

	"A manually generated remote tree" must {
		"work the same as the automatically generated tree" in {

			runOn(node1) {
				import idb.syntax.iql._

				// will run the Table and the Selection (sent from node2)
				val db = BagTable.empty[Int]
				REMOTE DEFINE (db, "db")

				enterBarrier("deployed")
				println("### DEPLOYED ###")

				Thread.sleep(10000)

				enterBarrier("sending")
				println("### SENDING ###")

				db += 1
				db += 2
				db += 3
				db += 4
				db += 5
				db += 6

				enterBarrier("finished")
				Predef.println("### FINISHED ###")
			}

			runOn(node2) {
				import idb.syntax.iql._
				import IR._

				// will send the Selection to node 1 and receive the final results
				enterBarrier("deployed")
				Predef.println("### DEPLOYED ###")
				val table : Rep[Query[Int]] = REMOTE GET [Int] (host1, "db", Taint("red"))

				val q1 : Rep[Query[Int]] = SELECT (*) FROM RECLASS (table, Taint("red")) WHERE ((i : Rep[Int]) => i > 4)
				val q2 : Rep[Query[Int]] = SELECT ((i : Rep[Int]) => i * 10) FROM RECLASS (q1, Taint("blue"))
				val q3 : Rep[Query[Int]] = RECLASS(q2, Taint("blue"))

				//Print the LMS tree representation
				val printer = new RelationalAlgebraPrintPlan {
					override val IR = idb.syntax.iql.IR
				}
				Predef.println("### Relation.tree ###\n" + printer.quoteRelation(root(q3, host2)))

				/*
					TODO: Get current host automatically? The host is only used to check colors and can thus be arbitrarily
					chosen, if you want a different color.
				*/
//				val ref = RemoteUtils.deploy(system, node(node2))(q3)
//				val r : Relation[Int] = RemoteUtils.fromWithDeploy(system, ref) //ROOT (host2, q3)

				val r : Relation[Int] = ROOT (host2, q3)



				idb.util.printEvents(r, "result")
				r.print()

				Thread.sleep(10000) // wait until ObservableHost has its observer registered

				enterBarrier("sending")
				Predef.println("### SENDING ###")

				Thread.sleep(2000)

				enterBarrier("finished")
				Predef.println("### FINISHED ###")


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
//
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



			}

			//
		}
	}
}

