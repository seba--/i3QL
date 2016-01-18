package sae.playground.remote

import akka.actor.{ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.SetTable
import idb.remote.{Added, RemoteTable, RemoteView, SendToRemote}

class SimpleRemoteTableTestMultiJvmNode1 extends SimpleRemoteTableTest

class SimpleRemoteTableTestMultiJvmNode2 extends SimpleRemoteTableTest

object SimpleRemoteTableTest {
}

class SimpleRemoteTableTest extends MultiNodeSpec(MultiNodeSampleConfig)
with STMultiNodeSpec with ImplicitSender {

  import MultiNodeSampleConfig._
  import SimpleRemoteTableTest._

  def initialParticipants = roles.size

  "A MultiNodeSample" must {

    "wait for all nodes to enter a barrier" in {
      enterBarrier("startup")
    }

    "send to and receive from a remote node" in {
      runOn(node1) {
        val db = SetTable.empty[String]

        val actor = system.actorOf(Props(classOf[RemoteTable[String]], db), "db")
        enterBarrier("deployed")

        enterBarrier("sending")
        Thread.sleep(1000)
        println(db.hasObservers)

        db += "Test1"
        db += "Test2"

        enterBarrier("receiving")
      }

      runOn(node2) {
        enterBarrier("deployed")

        val remoteHostPath: ActorPath = node(node1) / "user" / "db"
        val remoteView: RemoteView[String] = RemoteView[String](system, remoteHostPath, true)

        remoteView.addObserver(new SendToRemote[String](testActor))
        enterBarrier("sending")

        enterBarrier("receiving")

        import scala.concurrent.duration._
        expectMsg(1000.seconds, Added("Test1"))
        expectMsg(1000.seconds, Added("Test2"))


      }

      enterBarrier("finished")
    }
  }
}

