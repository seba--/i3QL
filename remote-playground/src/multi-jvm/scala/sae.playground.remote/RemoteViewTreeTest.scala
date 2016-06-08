package sae.playground.remote

import akka.actor.{Address, ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.BagTable
import idb.operators.impl.{ProjectionView, SelectionView}
import idb.remote._

class RemoteViewTreeTestMultiJvmNode1 extends RemoteViewTreeTest
class RemoteViewTreeTestMultiJvmNode2 extends RemoteViewTreeTest
object RemoteViewTreeTest {} // this object is necessary for multi-node testing

class RemoteViewTreeTest extends MultiNodeSpec(MultiNodeConfig)
with STMultiNodeSpec with ImplicitSender {

  import MultiNodeConfig._
  import RemoteViewTreeTest._

  def initialParticipants = roles.size

  "A RemoteView" must {
    "work in more complex trees" in {
      //enterBarrier("startup") // TODO: is this necessary?

      runOn(node1) {
        // will run the Table and the Selection (sent from node2)
        val db = BagTable.empty[Int]

        system.actorOf(Props(classOf[ObservableHost[Int]], db), "db") // TODO: provide easier way to create a remotely observable data source
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

        val remoteHostPath: ActorPath = node(node1) / "user" / "db"
        val fun: Int => Boolean = i => (i % 2) == 0

        // data flows from node1 (Table) -> node2 -> node1 -> node2
        val tree = RemoteView(
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
        )

        ObservableHost.forward(tree, system) // FIXME: always call this on the root node after tree construction (should happen automatically)
        tree.addObserver(new SendToRemote[String](testActor))

        enterBarrier("sending")

        import scala.concurrent.duration._
        expectMsg(10.seconds, Added("22"))
        expectMsg(10.seconds, Added("44"))
        expectMsg(10.seconds, Added("66"))
      }

      //enterBarrier("finished")
    }
  }
}

