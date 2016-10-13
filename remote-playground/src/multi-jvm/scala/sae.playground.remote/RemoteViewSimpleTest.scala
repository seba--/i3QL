package sae.playground.remote

import akka.actor.{ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.BagTable
import idb.remote._
import idb.syntax.iql.compilation.RemoteActor


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
      //enterBarrier("startup") // TODO: is this necessary?

      runOn(node1) {
        val db = BagTable.empty[String]

        system.actorOf(Props(classOf[RemoteActor[String]], db), "db") // TODO: provide easier way to create a remotely observable data source
        enterBarrier("deployed")

        enterBarrier("sending")
        Thread.sleep(100) // wait until ObservableHost has its observer registered
        println("has observers: " + db.hasObservers)

        db += "Test1"
        db += "Test2"
      }

      runOn(node2) {
        enterBarrier("deployed")

        val remoteHostPath: ActorPath = node(node1) / "user" / "db"
        val remoteView: ReceiveView[String] = ReceiveView[String](system, remoteHostPath, false)

        RemoteActor.forward(system, remoteView) // FIXME: always call this on the root node after tree construction (should happen automatically)
        new SendView[String](remoteView, testActor)

        enterBarrier("sending")

        import scala.concurrent.duration._
        expectMsg(10.seconds, Added("Test1"))
        expectMsg(10.seconds, Added("Test2"))
      }

      //enterBarrier("finished")
    }
  }
}

