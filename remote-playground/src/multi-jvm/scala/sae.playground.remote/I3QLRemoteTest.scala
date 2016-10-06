package sae.playground.remote

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
import idb.syntax.iql.RECLASS
import idb.syntax.iql.compilation.RemoteActor


import scala.virtualization.lms.common.{ScalaOpsPkgExp, StaticDataExp, StructExp, TupledFunctionsExp}

class I3QLRemoteTestMultiJvmNode1 extends I3QLRemoteTest
class I3QLRemoteTestMultiJvmNode2 extends I3QLRemoteTest
object I3QLRemoteTest {} // this object is necessary for multi-node testing

class I3QLRemoteTest extends MultiNodeSpec(MultiNodeConfig)
	with STMultiNodeSpec with ImplicitSender {

	import MultiNodeConfig._
	import I3QLRemoteTest._

	def initialParticipants = roles.size

	//Setup query environment
	val host1 = RemoteHost("node1", node(node1))
	val host2 = RemoteHost("node2", node(node2))

	implicit val env = QueryEnvironment.create(
		system,
		Map(host1 -> Set("red"), host2 -> Set("blue"))
	)

	"OOPS A RemoteView" must {
		"work in more complex trees" in {
			//enterBarrier("startup") // TODO: is this necessary?

			runOn(node1) {
				// will run the Table and the Selection (sent from node2)

				//system.actorOf(Props(classOf[ObservableHost[Int]], db), "db") // TODO: provide easier way to create a remotely observable data source
				import idb.syntax.iql._

				val db = BagTable.empty[Int]

				REMOTE RELATION (db, "db")

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

				import idb.syntax.iql._
				import idb.syntax.iql.IR._

				//FIXME: Why do we have to explicitly specify the type here?
				val table : Rep[Query[Int]] =
						REMOTE FROM [Int] (host1, "db", Color("red"))

				val q1 = SELECT (*) FROM RECLASS(table, Color("red")) WHERE ((i : Rep[Int]) => i > 2)
					//SELECT ((i : Rep[Int]) => i + 2) FROM RECLASS(table, Color("blue"))

				val q2_0 = //RECLASS(q1, Color("blue"))
					q1

				val q2 = SELECT ((i : Rep[Int]) => i + 2) FROM RECLASS (q2_0, Color("blue"))
						//SELECT ((i : Rep[Int]) => i.doubleValue())
						//SELECT (*)

				val q3_0 = RECLASS(q2, Color("red"))
					//q2

				val q3 = ROOT (RECLASS(q3_0, Color("blue")), host2)

				val printer = new RelationalAlgebraPrintPlan {
					override val IR = idb.syntax.iql.IR
				}

				Predef.println(printer.quoteRelation(q3))

				val r : Relation[_] = q3
				RemoteActor.forward(system, r)
				val relation = r.asMaterialized

				Predef.println(relation.prettyprint(" "))

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
				)    */

				//ObservableHost.forward(tree, system) // FIXME: always call this on the root node after tree construction (should happen automatically)
				relation.addObserver(new Send(testActor))

				enterBarrier("sending")

				try {
					import scala.concurrent.duration._
					expectMsg(10.seconds, Added(5))
					expectMsg(10.seconds, Added(6))
					expectMsg(10.seconds, Added(7))
					expectMsg(10.seconds, Added(8))
				} finally {
					Thread.sleep(7000)
					Predef.println("RELATION:")
					relation.foreach(Predef.println)
				}


			}

			//enterBarrier("finished")
		}
	}
}

