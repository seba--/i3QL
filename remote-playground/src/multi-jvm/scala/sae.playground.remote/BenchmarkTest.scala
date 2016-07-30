package sae.playground.remote

import java.io.FileOutputStream

import akka.actor
import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.BagTable
import idb.observer.Observer
import idb.remote._
import idb.operators.impl.SelectionView

// change the number of BenchmarkTestMultiJvmNode classes here (by disabling some of the nodes)
// as well as the number of roles in BenchmarkConfig to configure the benchmark
class BenchmarkTestMultiJvmNode1 extends BenchmarkTest
class BenchmarkTestMultiJvmNode2 extends BenchmarkTest
class BenchmarkTestMultiJvmNode3 extends BenchmarkTest
class BenchmarkTestMultiJvmNode4 extends BenchmarkTest
class BenchmarkTestMultiJvmNode5 extends BenchmarkTest
class BenchmarkTestMultiJvmNode6 extends BenchmarkTest
class BenchmarkTestMultiJvmNode7 extends BenchmarkTest
class BenchmarkTestMultiJvmNode8 extends BenchmarkTest
class BenchmarkTestMultiJvmNode9 extends BenchmarkTest
class BenchmarkTestMultiJvmNode10 extends BenchmarkTest
object BenchmarkTest {} // this object is necessary for multi-node testing


object Util {
  def fib(n: Int): Int = n match {
    case 0 | 1 => n
    case _ => fib(n - 1) + fib(n - 2)
  }

  def isPrime(n: Int): Boolean = {
    for (d <- 2 until n) {
      if (n % d == 0) { return false }
    }
    true
  }
}

class BenchmarkTest extends MultiNodeSpec(BenchmarkConfig)
with STMultiNodeSpec with ImplicitSender {

  type D = (Int, Long)

  import BenchmarkConfig._
  import BenchmarkTest._

  def initialParticipants = roles.size


  "A RemoteView" must {
    "run this Benchmark" in {
      runOn(nodes(0)) {
        val out: java.io.PrintStream = System.out; //new java.io.PrintStream(new FileOutputStream("benchmark.txt", true))
        out.println("Running benchmark with " + BenchmarkConfig.nodes.size + " nodes ...")

        val beforeInitTime = System.nanoTime()

        val db = BagTable.empty[D]

        system.actorOf(Props(classOf[ObservableHost[D]], db), "db")

        val remoteHostPath = node(nodes(0)) / "user" / "db"

        var tree = RemoteView[D](system, remoteHostPath, false)
        val selectionFun: D => Boolean = { case (idx, time) => true /*Util.isPrime(303029)*/ }

        for (i <- 1 to BenchmarkConfig.nodes.size - 1) {
          tree = RemoteView[D](system, node(nodes(i)).address, new SelectionView(tree, selectionFun, false))
        }

        tree = RemoteView[D](system, node(nodes(0)).address, tree)

        ObservableHost.forward(tree, system)
        tree.addObserver(new Observer[D] {
          override def added(v: D) = {
            val (idx, time) = v
            if (idx == 1) {
              val currentTime = System.nanoTime()
              val timeElapsed = (currentTime - time).toDouble / 1000 / 1000
              out.print(s"${"=" * 50}\n** Roundtrip time (first): $timeElapsed")
            } else if (idx == 10) {
              val currentTime = System.nanoTime()
              val timeElapsed = (currentTime - time).toDouble / 1000 / 1000
              out.println(s"\t (last) $timeElapsed\n${"=" * 50}")
            }
          }
          override def removed(v: D) = {}
          override def updated(oldV: D, newV: D) = {}
          override def addedAll(vs: Seq[D]) = {}
          override def removedAll(vs: Seq[D]) = {}
          override def endTransaction() = {}
        })

        tree.addObserver(new SendToRemote[D](testActor))
        Thread.sleep(100) // wait until setup complete

        val afterInitTime = System.nanoTime()
        out.println(s"${"="*50}\n** Initialization time: ${((afterInitTime - beforeInitTime).toDouble / 1000 / 1000)}ms\n${"="*50}")

        val currentTime = System.nanoTime()
        var countWarmup = 0
        // warmup for 10 seconds
        while(System.nanoTime() < currentTime + 10*1000*1000*1000) {
          db +=(-1, currentTime) // -1 is marker for warmup
          countWarmup += 1
          import scala.concurrent.duration._
          expectMsg(50.seconds, Added((-1, currentTime)))
        }
        out.println(s"${"="*50}\n** Ran $countWarmup warmup iterations.\n${"="*50}")

        for (i <- 1 to 100) {
          val currentTime = System.nanoTime()
          for (n <- 1 to 10) {
            db += (n, currentTime)
          }
          import scala.concurrent.duration._
          for (n <- 1 to 10) {
            expectMsg(50.seconds, Added((n, currentTime)))
          }
        }
      }

      // needed to keep other hosts running
      enterBarrier("finished")
    }
  }
}